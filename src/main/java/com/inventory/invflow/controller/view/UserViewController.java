package com.inventory.invflow.controller.view;

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

import com.inventory.invflow.dto.user.AdminResetPassword;
import com.inventory.invflow.dto.user.UserRegisterRequest;
import com.inventory.invflow.dto.user.UserResponse;
import com.inventory.invflow.enums.UserRole;
import com.inventory.invflow.exception.BadCredentialsException;
import com.inventory.invflow.service.UserService;

import jakarta.validation.Valid;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/users")
public class UserViewController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listUsers (
        @RequestParam(name = "q", required = false) String keyword, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> users = userService.getAllUsers(pageable);

        if(keyword == null || keyword.isBlank()) {
            users = userService.getAllUsers(pageable);
        } else {
            users = userService.searchUsers(keyword, pageable);
        }

        model.addAttribute("pageTitle", "帳號管理");
        model.addAttribute("activePage", "users");
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);

        return "user/list";
    }

    @GetMapping("/{userName}/status")
    public String toggleUserStatus(@PathVariable String userName, RedirectAttributes redirectAttributes) {

        UserResponse user = userService.getUserByUserName(userName);

        if(Boolean.TRUE.equals(user.enabled())) {
            user = userService.disable(userName);
            redirectAttributes.addFlashAttribute("successMessage", "已停用");
        } else {
            user = userService.enable(userName);
            redirectAttributes.addFlashAttribute("successMessage", "已啟用");
        }

        return "redirect:/users";
    }

    @GetMapping("/status")
    public String listUsers(
        @RequestParam(required = false) Boolean enabled, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> users = userService.listUsers(enabled, pageable);
        model.addAttribute("users", users);
        model.addAttribute("enabled", enabled);

        return "user/list";
    }

    // 新增 user
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        
        model.addAttribute("pageTile", "新增帳號");
        model.addAttribute("activePage", "帳號");

        if(!model.containsAttribute("userForm")) {

            UserRegisterRequest emptyForm = new UserRegisterRequest(
                null, 
                null, 
                null
            );
            model.addAttribute("userForm", emptyForm);
        }

        return "user/create";
    }

    @PostMapping("/create")
    public String handleCreate(
        @Valid @ModelAttribute("userForm") UserRegisterRequest userForm, 
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {

            model.addAttribute("pageTile", "新增帳號");
            model.addAttribute("activePage", "帳號");

            return "user/create";
        }

        try {
            userService.register(userForm);
        } catch (BadCredentialsException e) {
            bindingResult.rejectValue("userName", "buiness.invalid", e.getMessage());
            return "user/create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "帳號新增成功");
        return "redirect:/users";
    }

    // 編輯頁面
    @GetMapping("/{userName}/edit")
    public String editUser(@PathVariable String userName, Model model) {

        UserResponse user = userService.getUserByUserName(userName);

        model.addAttribute("pageTitle", "編輯帳號");
        model.addAttribute("activePage", "users");
        model.addAttribute("user", user);

        AdminResetPassword resetPasswordForm = new AdminResetPassword(userName);
        model.addAttribute("resetPasswordForm", resetPasswordForm);

        return "user/edit";
    }

    // 調整權限
    @PostMapping("/{userName}/role")
    public String updateUserRole(@PathVariable String userName,
                                 @RequestParam("role") UserRole userRole,
                                 RedirectAttributes redirectAttributes) {

        userService.updateUserRole(userName, userRole);
        redirectAttributes.addFlashAttribute("successMessage", "權限調整成功");

        return "redirect:/users";
    }
    
    // 重設密碼
    @PostMapping("/{userName}/password-reset")
    public String resetPassword(
        @PathVariable String userName, 
        @Valid @ModelAttribute("resetPasswordForm") AdminResetPassword form,
        BindingResult bindingResult, RedirectAttributes redirectAttributes,
        Model model) {

            if(bindingResult.hasErrors()) {
                model.addAttribute("user", userService.getUserByUserName(userName));
                return "user/edit";
            }

            userService.resetPassword(userName, form);
            redirectAttributes.addFlashAttribute("successMessage", "已成功重設密碼");

            return "redirect:/users/" + userName + "/edit";
        }
}
