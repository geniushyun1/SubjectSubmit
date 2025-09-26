package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                String role = userDetails.getUser().getRole().name();
                
                if ("PROFESSOR".equals(role)) {
                    return "redirect:/professor/dashboard";
                } else if ("STUDENT".equals(role)) {
                    return "redirect:/student/dashboard";
                }
            } catch (ClassCastException e) {
                // Principal이 UserDetailsImpl이 아닌 경우 처리
                return "redirect:/login";
            }
        }
        return "redirect:/login";
    }
}
