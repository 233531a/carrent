package com.example.carrent.controller;

import com.example.carrent.service.UserService;
import com.example.carrent.web.dto.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    @GetMapping("/login")
    public String login() {
        return "login"; // login.html
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegistrationForm form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        // после успешной регистрации отправим на логин
        return "redirect:/login?registered";
    }
}
