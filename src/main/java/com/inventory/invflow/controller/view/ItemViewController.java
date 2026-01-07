package com.inventory.invflow.controller.view;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.inventory.invflow.dto.item.ItemCreateRequest;
import com.inventory.invflow.dto.item.ItemResponse;
import com.inventory.invflow.dto.item.ItemUpdateRequest;
import com.inventory.invflow.dto.supplier.SupplierResponse;
import com.inventory.invflow.enums.WineType;
import com.inventory.invflow.exception.DuplicateResourceException;
import com.inventory.invflow.service.ItemService;
import com.inventory.invflow.service.SupplierService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/items")
public class ItemViewController {
    
    @Autowired
    private ItemService itemService;

    @Autowired
    private SupplierService supplierService;
    

    // 商品列表頁
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String listItems(
        @RequestParam(name = "q", required = false) String keyword, 
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
        ) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ItemResponse> items = itemService.getAllItem(pageable);

        if(keyword == null || keyword.isBlank()) {
            items = itemService.getAllItem(pageable);
        } else {
            items = itemService.searchItems(keyword, pageable);
        }

        model.addAttribute("pageTitle", "商品清單");
        model.addAttribute("activePage", "品項");
        model.addAttribute("items", items);
        model.addAttribute("q", keyword);

        return "item/list";
    }

    // 查詢 啟用/停用 商品
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String listItems(
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ItemResponse> items = itemService.listItems(enabled, pageable);
        model.addAttribute("items", items);
        model.addAttribute("enabled", enabled);

        return "item/list";
    }

    // 單一商品頁
    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String itemDetail(@PathVariable String itemId, Model model) {

        ItemResponse item = itemService.getItemById(itemId);

        model.addAttribute("pageTitle", "商品詳情 - " + itemId);
        model.addAttribute("activePage", "items");
        model.addAttribute("item", item);

        return "item/detail";
    }

    // 新增商品頁面
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String showCreateForm(Model model) {

        model.addAttribute("pageTitle", "新增品項");
        model.addAttribute("activePage", "品項");

        if(!model.containsAttribute("itemForm")) {

            ItemCreateRequest emptyForm = new ItemCreateRequest(
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null
            );
            model.addAttribute("itemForm", emptyForm);
        }

        model.addAttribute("wineTypes", WineType.values());
        List<SupplierResponse> suppliers = supplierService.getEnabledSuppliers();
        model.addAttribute("suppliers", suppliers);

        return "item/create";
    }

    // 接收表單資料，呼叫 service 建立品項
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String handleCreate(
        @Valid @ModelAttribute("itemForm") ItemCreateRequest itemForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
        ) {
        
        if(bindingResult.hasErrors()) {

            model.addAttribute("pageTitle", "新增品項");
            model.addAttribute("activePage", "品項");
            model.addAttribute("wineTypes", WineType.values());
            model.addAttribute("suppliers", supplierService.getEnabledSuppliers());
            
            return "item/create";
        }

        try {
            itemService.createItem(itemForm);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("name", "business.invalid", e.getMessage());
            model.addAttribute("wineTypes", WineType.values());
            model.addAttribute("suppliers", supplierService.getEnabledSuppliers());
            return "item/create";
        }
    
        
        redirectAttributes.addFlashAttribute("successMessage", "品項建立成功");
        return "redirect:/items";
    }

    // 編輯商品
    @GetMapping("/{itemId}/edit")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String editItem(@PathVariable String itemId, Model model) {

        ItemResponse item = itemService.getItemById(itemId);

        ItemUpdateRequest form = new ItemUpdateRequest(
                item.name(),
                item.originCountry(),
                item.vintage()
        );

        model.addAttribute("itemId", itemId);
        model.addAttribute("form", form);

        return "item/edit";
    }

    // 接收表單資料，呼叫 service 編輯品項
    @PostMapping("/{itemId}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','OPERATOR')")
    public String handleEdit(
        @PathVariable String itemId, 
        @Valid @ModelAttribute("form") ItemUpdateRequest form,
        BindingResult bindingResult,
        Model model
        ) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("itemId", itemId);
            model.addAttribute("form", form);

            return "item/edit";
        }

        itemService.updatePartial(itemId, form);

        return "redirect:/items/" + itemId;
    }

    // 啟用 / 停用商品
    @PostMapping("/{itemId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String toggleItemStatus(@PathVariable String itemId, RedirectAttributes redirectAttributes) {

        ItemResponse item = itemService.getItemById(itemId);

        if(Boolean.TRUE.equals(item.enabled())) {
            itemService.disableItem(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "已停用");
        } else {
            itemService.enableItem(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "已啟用");
        }
        return "redirect:/items/" + itemId;
    }
}
