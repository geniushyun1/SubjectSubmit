package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }
        return "login";
    }
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        
        // 사용자명 중복 확인
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "이미 사용 중인 사용자명입니다.");
            return "register";
        }
        
        // 이메일 중복 확인
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "이미 사용 중인 이메일입니다.");
            return "register";
        }
        
        try {
            System.out.println("회원가입 시도: " + user.getUsername() + " (" + user.getEmail() + ")");
            User savedUser = userService.createUser(user);
            System.out.println("회원가입 성공: " + savedUser.getUsername() + " (ID: " + savedUser.getId() + ")");
            model.addAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "login";
        } catch (Exception e) {
            System.err.println("회원가입 실패: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "register";
        }
    }
}
