package com.example.carrent.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 64)
    private String make;

    @NotBlank @Size(max = 64)
    private String model;

    @NotBlank @Size(max = 32)
    @Column(name = "vehicle_class")
    private String vehicleClass; // economy / business / suv

    @NotBlank @Size(max = 8)
    private String transmission; // AT / MT

    @Min(1980) @Max(2100)
    private int year;

    @NotNull
    @DecimalMin(value = "1.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    @Column(name = "daily_price", precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    private boolean available;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog", nullable = false)
    private CatalogType catalog = CatalogType.REGULAR;

    // getters/setters

    public CatalogType getCatalog() { return catalog; }
    public void setCatalog(CatalogType catalog) { this.catalog = catalog; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getVehicleClass() { return vehicleClass; }
    public void setVehicleClass(String vehicleClass) { this.vehicleClass = vehicleClass; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BigDecimal getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(BigDecimal dailyPrice) { this.dailyPrice = dailyPrice; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
