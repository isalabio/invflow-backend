package com.inventory.invflow.controller.view;

import com.inventory.invflow.dto.supplier.SupplierCreateRequest;
import com.inventory.invflow.dto.supplier.SupplierResponse;
import com.inventory.invflow.dto.supplier.SupplierUpdateRequest;
import com.inventory.invflow.exception.DuplicateResourceException;
import com.inventory.invflow.service.SupplierService;

import jakarta.validation.Valid;

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


@Controller
@RequestMapping("/suppliers")
public class SupplierViewController {
    
    @Autowired
    private SupplierService supplierService;



    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String listSuppliers(
        @RequestParam(name="q", required = false) String keyword, 
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
        ) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SupplierResponse> suppliers = supplierService.searchSuppliers(keyword, enabled, pageable);
        
            
        model.addAttribute("pageTitle", "供應商清單");
        model.addAttribute("activePage", "suppliers");
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("q", keyword);
        model.addAttribute("enabled", enabled);

        return "supplier/list";
    }

    // 查詢 啟用/停用 廠商
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String listSuppliers(
        @RequestParam(required = false) Boolean enabled, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SupplierResponse> suppliers = supplierService.listSuppliers(enabled, pageable);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("enabled", enabled);

        return "supplier/list";
    }

    // 廠商詳情
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public String supplierDetail(@PathVariable String supplierId, Model model) {
        
        SupplierResponse supplier = supplierService.getSupplierById(supplierId);

        model.addAttribute("pageTitle", "供應商詳情 - " + supplierId + " - " + supplier.supplierName());
        model.addAttribute("activePate", "suppliers");
        model.addAttribute("supplier", supplier);

        return "supplier/detail";
    }

    // 新增廠商頁面
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String showCreateForm(Model model) {

        model.addAttribute("pageTitle", "新增廠商");
        model.addAttribute("activePage", "廠商");
        
        if(!model.containsAttribute("supplierForm")) {
            SupplierCreateRequest emptyForm = new SupplierCreateRequest(
                null, 
                null, 
                null, 
                null, 
                null, 
                null
            );
            model.addAttribute("supplierForm", emptyForm);
        } 
        return "supplier/create";
    }
    
    // 接收表單資料，呼叫 service 新增廠商
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public String handleCreate(
        @Valid @ModelAttribute("supplierForm") SupplierCreateRequest supplierForm, 
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
        ) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("pageTitle", "新增廠商");
            model.addAttribute("activePage", "廠商");

            return "supplier/create";
        }

        try {
            supplierService.createSupplier(supplierForm);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("supplierName", "business.invalid", e.getMessage());
            return "supplier/create";
        } 
        

        redirectAttributes.addFlashAttribute("successMessage", "廠商建新成功");
        return "redirect:/suppliers";
    }

    // 編輯廠商
    @GetMapping("/{supplierId}/edit")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public String editSupplier(@PathVariable String supplierId, Model model) {

        SupplierResponse supplier = supplierService.getSupplierById(supplierId);

        SupplierUpdateRequest form = new SupplierUpdateRequest(
                supplier.supplierName(),
                supplier.contactName(),
                supplier.phone(),
                supplier.email(),
                supplier.paymentTerm(),
                supplier.note());

        model.addAttribute("supplierId", supplierId);
        model.addAttribute("form", form);

        return "supplier/edit";
    }

    // 接收表單資料，呼叫 service 修改廠商
    @PostMapping("/{supplierId}/edit")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public String handleEdit(
        @PathVariable String supplierId, 
        @Valid @ModelAttribute("form") SupplierUpdateRequest form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("supplierId", supplierId);
            model.addAttribute("form", form);

            return "supplier/edit";

        }

        try {
            supplierService.updatePartial(supplierId, form);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue("supplierName", "duplicate", e.getMessage());
            return "supplier/edit";
        }
        
        return "redirect:/suppliers/" + supplierId;
    }

    // 啟用 / 停用廠商
    @PostMapping("/{supplierId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String toggleSupplierStatus(
        @PathVariable String supplierId, 
        RedirectAttributes redirectAttributes
        ) {

        SupplierResponse supplier = supplierService.getSupplierById(supplierId);

        if(Boolean.TRUE.equals(supplier.enabled())) {
            supplierService.disableSupplier(supplierId);
            redirectAttributes.addFlashAttribute("successMessage", "已停用");
        } else {
            supplierService.enableSupplier(supplierId);
            redirectAttributes.addFlashAttribute("successMessage", "已啟用");
        }

        return "redirect:/suppliers/" + supplierId;
    }
}
