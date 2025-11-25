package com.example.carrent.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) для REST API автомобилей.
 *
 * Используется для передачи данных об автомобиле между клиентом и сервером
 * через REST API. Отличается от модели Car тем, что:
 * - Использует "brand" вместо "make" (более понятное название для API)
 * - Не содержит служебных полей (createdAt, updatedAt)
 * - Содержит валидационные аннотации для проверки данных
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
public class CarDTO {

    private Long id;

    @NotBlank(message = "Brand is required")
    @Size(max = 64, message = "Brand must not exceed 64 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 64, message = "Model must not exceed 64 characters")
    private String model;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "1.0", message = "Price must be at least 1.0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal pricePerDay;

    @NotBlank(message = "Vehicle class is required")
    @Size(max = 32, message = "Vehicle class must not exceed 32 characters")
    private String vehicleClass;

    @NotBlank(message = "Transmission is required")
    @Size(max = 8, message = "Transmission must not exceed 8 characters")
    private String transmission;

    @Min(value = 1980, message = "Year must be at least 1980")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private int year;

    private boolean available;

    private String photoUrl;

    private String catalog;

    // ========== GETTERS ==========

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public String getTransmission() {
        return transmission;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getCatalog() {
        return catalog;
    }

    // ========== SETTERS ==========

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
}
