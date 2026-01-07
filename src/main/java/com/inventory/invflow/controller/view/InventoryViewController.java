package com.inventory.invflow.controller.view;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inventory.invflow.dto.inventoryLog.InventoryAdjustmentRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogResponse;
import com.inventory.invflow.dto.inventoryLog.InventorySummaryResponse;
import com.inventory.invflow.enums.MovementType;
import com.inventory.invflow.exception.ResourceNotFoundException;
import com.inventory.invflow.service.InventoryLogService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/inventorylogs")
public class InventoryViewController {
    
    @Autowired
    private InventoryLogService inventoryLogService;

    // 顯示最近異動
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String listLogs(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) MovementType type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {

        LocalDate maxDate = LocalDate.now();
        LocalDate minDate = maxDate.minusYears(1);

        LocalDate start = (startDate != null) ? startDate : minDate;
        LocalDate end = (endDate != null ) ? endDate : maxDate;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryLogResponse> logs = inventoryLogService.searchLogs(keyword, type, start, end, pageable);


        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", end);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("movementTypes", MovementType.values());
        model.addAttribute("showBack", false);

        return "inventory/list";
    }

    // 新增異動紀錄
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String showCreateForm(Model model) {

        List<MovementType> movementTypes = 
            Arrays.stream(MovementType.values())
                  .filter(MovementType::isAllowedForCreateLog)
                  .toList();

        model.addAttribute("pageTitle", "庫存變動明細");
        model.addAttribute("activePage", "明細");
        model.addAttribute("movementTypes", movementTypes);

        if(!model.containsAttribute("inventoryForm")) {
            InventoryLogRequest emptyForm = new InventoryLogRequest(
                null, 
                null, 
                null, 
                null
            );
            model.addAttribute("inventoryForm", emptyForm);
        }

        return "inventory/create";
    }

    // 接收表單資料，呼叫 service 新增異動明細
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String handleCreate(
        @Valid @ModelAttribute("inventoryForm") InventoryLogRequest inventoryForm, 
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("papeTitle", "庫存變動明細");
            model.addAttribute("activePage", "明細");
            model.addAttribute("movementTypes", MovementType.values());

            return "inventory/create";
        }

        try {
            inventoryLogService.createLog(inventoryForm);
        } catch (ResourceNotFoundException e) {
            bindingResult.rejectValue("itemId", "business.invalid", e.getMessage());
            model.addAttribute("movementTypes", MovementType.values());
            return "inventory/create";
        } 
        catch (IllegalArgumentException e) {
            bindingResult.rejectValue("changeQuantity", "business.invalid", e.getMessage());
            model.addAttribute("movementTypes", MovementType.values());
            return "inventory/create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "明細新增成功");
        return "redirect:/inventorylogs";
    }

    // 新增調整庫存記錄
    @GetMapping("/adjust")
    @PreAuthorize("hasRole('MANAGER')")
    public String showAdjustForm(Model model) {

        model.addAttribute("pageTitle", "庫存調整");
        model.addAttribute("activePage", "調整");

        if(!model.containsAttribute("adjustForm")) {
            InventoryAdjustmentRequest emptyForm = new InventoryAdjustmentRequest(
                null, 
                null, 
                null
            );
            model.addAttribute("adjustForm", emptyForm);
        }

        return "inventory/adjust";
    }

    // 接收表單資料，呼叫 service 新增調整明細
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('MANAGER')")
    public String handleAdjust(
        @Valid @ModelAttribute("adjustForm") InventoryAdjustmentRequest emptyForm, 
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("pageTitle", "庫存調整");
            model.addAttribute("activePage", "調整");

            return "inventory/adjust";
        }

        try {
            inventoryLogService.adjustStock(emptyForm);
        } catch (ResourceNotFoundException e) {
            bindingResult.rejectValue("itemId", "business.invalid", e.getMessage());
            return "inventory/adjust";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("actualStock", "business.invalid", e.getMessage());
            return "inventory/adjust";
        }

        redirectAttributes.addFlashAttribute("successMessage", "已儲存");
        return "redirect:/inventorylogs";
    }

    @GetMapping("/{itemId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String summaryByItem(
        @PathVariable String itemId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Model model) {

            LocalDate maxDate = LocalDate.now();
            LocalDate minDate = maxDate.minusYears(1);

            LocalDate start = (startDate != null) ? startDate : minDate;
            LocalDate end = (endDate != null) ? endDate : maxDate;

            InventorySummaryResponse summary = inventoryLogService.getSummaryByItem(itemId, start, end);

            model.addAttribute("itemId", itemId);
            model.addAttribute("itemName", summary.name());
            model.addAttribute("summary", summary);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("showBack", true);

        return "inventory/summary";
    }

    @GetMapping("/summaryAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String summaryAll(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model
    ) {
        
        LocalDate maxDate = LocalDate.now();
        LocalDate minDate = maxDate.minusYears(1);

        LocalDate start = (startDate != null) ? startDate : minDate;
        LocalDate end = maxDate;

        List<InventorySummaryResponse> summaryAll = inventoryLogService.getSummaryAll(start, end);

        model.addAttribute("summaryAll", summaryAll);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("showBack", true);

        return "inventory/summary-all";
    }
}
