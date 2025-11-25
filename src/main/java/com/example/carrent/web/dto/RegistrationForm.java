package com.example.carrent.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для формы регистрации пользователя.
 *
 * Используется для передачи данных из формы регистрации в контроллер.
 * Содержит валидационные аннотации для проверки корректности введенных данных.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
public class RegistrationForm {

    /**
     * Логин пользователя.
     * Должен быть уникальным в системе.
     */
    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    /**
     * Пароль пользователя.
     * Минимальная длина - 6 символов для безопасности.
     */
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /**
     * Подтверждение пароля.
     * Должен совпадать с полем password.
     */
    @NotBlank
    @Size(min = 6, max = 100)
    private String confirmPassword;

    // ========== GETTERS ==========

    /**
     * Получить логин пользователя.
     *
     * @return логин пользователя
     */
    public String getUsername() {
        return username;
    }

    /**
     * Получить пароль пользователя.
     *
     * @return пароль пользователя
     */
    public String getPassword() {
        return password;
    }

    /**
     * Получить подтверждение пароля.
     *
     * @return подтверждение пароля
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    // ========== SETTERS ==========

    /**
     * Установить логин пользователя.
     *
     * @param username логин пользователя
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Установить пароль пользователя.
     *
     * @param password пароль пользователя
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Установить подтверждение пароля.
     *
     * @param confirmPassword подтверждение пароля
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
