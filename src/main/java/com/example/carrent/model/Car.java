package com.example.carrent.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Модель автомобиля, который можно сдавать в аренду.
 *
 * Представляет автомобиль в системе аренды со всеми характеристиками:
 * - Производитель и модель
 * - Класс автомобиля и тип коробки передач
 * - Год выпуска
 * - Цена за день аренды
 * - Доступность для бронирования
 * - Раздел каталога (REGULAR, TAXI, DELIVERY)
 *
 * Автоматически управляет временными метками (createdAt, updatedAt).
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
@Entity
@Table(name = "cars")
public class Car {

    // ========== БАЗОВЫЕ ПОЛЯ ==========

    /**
     * Уникальный идентификатор автомобиля.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Производитель автомобиля (например, "Toyota", "BMW").
     */
    @NotBlank
    @Size(max = 64)
    private String make;

    /**
     * Модель автомобиля (например, "Camry", "X5").
     */
    @NotBlank
    @Size(max = 64)
    private String model;

    /**
     * Класс автомобиля (например, "economy", "business", "suv").
     */
    @NotBlank
    @Size(max = 32)
    @Column(name = "vehicle_class")
    private String vehicleClass;

    /**
     * Тип коробки передач (например, "AT" - автоматическая, "MT" - механическая).
     */
    @NotBlank
    @Size(max = 8)
    private String transmission;

    /**
     * Год выпуска автомобиля.
     * Должен быть в диапазоне от 1980 до 2100.
     */
    @Min(1980)
    @Max(2100)
    private int year;

    // ========== ФИНАНСОВЫЕ ПОЛЯ ==========

    /**
     * Цена за день аренды в рублях.
     * Минимальная цена - 1.0, точность - 2 знака после запятой.
     */
    @NotNull
    @DecimalMin(value = "1.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    @Column(name = "daily_price", precision = 10, scale = 2)
    private BigDecimal dailyPrice;

    // ========== СЛУЖЕБНЫЕ ПОЛЯ ==========

    /**
     * Доступность автомобиля для бронирования.
     * false - автомобиль недоступен (забронирован, на ремонте и т.д.)
     * true - автомобиль доступен для бронирования
     */
    private boolean available;

    /**
     * Дата и время создания записи об автомобиле.
     * Устанавливается автоматически при создании, не изменяется при обновлении.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата и время последнего обновления записи.
     * Обновляется автоматически при каждом изменении записи.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Обработчик события перед обновлением записи.
     * Автоматически обновляет поле updatedAt текущей датой и временем.
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== МЕДИА И КАТАЛОГ ==========

    /**
     * Относительный путь к изображению автомобиля.
     * Например, "/images/camry.png"
     */
    @Column(name = "photo_url")
    private String photoUrl;

    /**
     * Раздел каталога, в котором отображается автомобиль.
     * REGULAR - обычный каталог (доступен всем)
     * TAXI - каталог такси (только для сотрудников)
     * DELIVERY - каталог доставки (только для сотрудников)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "catalog", nullable = false)
    private CatalogType catalog = CatalogType.REGULAR;

    // --------- Getters / Setters ---------

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

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
