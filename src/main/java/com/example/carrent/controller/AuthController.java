package com.example.carrent.controller;

import com.example.carrent.service.UserService;
import com.example.carrent.web.dto.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для аутентификации и регистрации пользователей.
 *
 * Обрабатывает:
 * - Отображение страницы входа (логин обрабатывается Spring Security)
 * - Отображение формы регистрации
 * - Обработку регистрации новых пользователей
 *
 * @author Система аренды автомобилей
 */
@Controller
public class AuthController {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param userService сервис пользователей
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отобразить страницу входа.
     *
     * GET /login
     *
     * Обработка формы входа выполняется Spring Security (настроено в SecurityConfig).
     * Этот метод только отображает страницу.
     *
     * @return имя шаблона "login" (templates/login.html)
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Отобразить форму регистрации.
     *
     * GET /register
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "register" (templates/register.html)
     */
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    /**
     * Обработать регистрацию нового пользователя.
     *
     * POST /register
     *
     * Процесс:
     * 1. Валидация формы (через @Valid)
     * 2. Если есть ошибки валидации - возврат на форму
     * 3. Попытка регистрации через UserService
     * 4. При ошибке (логин занят) - возврат на форму с сообщением
     * 5. При успехе - редирект на страницу входа с параметром ?registered
     *
     * @param form форма регистрации с данными пользователя
     * @param binding результат валидации формы
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "register" при ошибке, редирект на /login?registered при успехе
     */
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
        return "redirect:/login?registered";
    }
}
