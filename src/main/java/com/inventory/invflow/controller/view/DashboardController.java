package com.inventory.invflow.controller.view;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inventory.invflow.dto.dashboard.DashboardView;
import com.inventory.invflow.impl.DashboardService;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
       DashboardView vm = dashboardService.getDashboard();

        model.addAttribute("vm", vm);
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");

        return "dashboard/index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
}
