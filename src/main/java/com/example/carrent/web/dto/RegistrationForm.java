package com.example.carrent.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank @Size(min=3, max=100)
    private String username;

    @NotBlank @Size(min=6, max=100)
    private String password;

    @NotBlank @Size(min=6, max=100)
    private String confirmPassword;

    // если хочешь использовать email как логин — замени поле username на email
    // @Email @NotBlank private String email;

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
