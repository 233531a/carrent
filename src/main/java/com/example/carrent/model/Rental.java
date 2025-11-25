package com.example.carrent.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Аренда/бронирование автомобиля.
 * Здесь фиксируем: кто взял, какую машину, на какие даты и за сколько.
 */
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --------- Связи ---------

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;             // какую машину арендовали

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;   // кто арендовал

    // --------- Даты аренды ---------

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // начало аренды

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;   // конец аренды

    // --------- Статус аренды ---------

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RentalStatus status = RentalStatus.PENDING; // по умолчанию — "ожидание"

    // --------- Денежные поля ---------

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;     // цена за день на момент брони

    @Column(name = "days_count", nullable = false)
    private Integer daysCount;          // сколько дней аренды

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;     // итоговая сумма

    // --------- Таймстемпы ---------

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public Rental() {
    }

    // =====================================================================
    // Жизненный цикл сущности: перед вставкой и перед обновлением
    // =====================================================================

    @PrePersist
    public void prePersist() {
        validateDates();
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = this.createdAt;

        // если цену/дни не передали — посчитаем сами
        if (this.pricePerDay == null && this.car != null) {
            this.pricePerDay = this.car.getDailyPrice();
        }
        if (this.daysCount == null && this.startDate != null && this.endDate != null) {
            long days = ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1; // включительно
            if (days <= 0) throw new IllegalArgumentException("Диапазон дат некорректен");
            this.daysCount = (int) days;
        }
        if (this.totalAmount == null) {
            if (this.pricePerDay == null || this.daysCount == null) {
                throw new IllegalStateException("Невозможно рассчитать totalAmount");
            }
            this.totalAmount = this.pricePerDay.multiply(BigDecimal.valueOf(this.daysCount));
        }
    }

    @PreUpdate
    public void preUpdate() {
        validateDates();
        this.updatedAt = java.time.LocalDateTime.now();

        // если поменяли даты — пересчитаем кол-во дней
        if (this.startDate != null && this.endDate != null) {
            long days = ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1;
            if (days <= 0) throw new IllegalArgumentException("Диапазон дат некорректен");
            this.daysCount = (int) days;
        }
        // если цену не задали явно — возьмём из машины
        if (this.pricePerDay == null && this.car != null) {
            this.pricePerDay = this.car.getDailyPrice();
        }
        // финальный пересчёт суммы
        if (this.pricePerDay != null && this.daysCount != null) {
            this.totalAmount = this.pricePerDay.multiply(BigDecimal.valueOf(this.daysCount));
        }
    }

    /**
     * Проверка, что даты заполнены и конец не раньше начала.
     */
    private void validateDates() {
        if (this.startDate == null || this.endDate == null) {
            throw new IllegalArgumentException("Даты бронирования обязательны");
        }
        if (this.endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Дата окончания раньше даты начала");
        }
    }

    // --------- Getters / Setters ---------

    public Long getId() { return id; }
    public Car getCar() { return car; }
    public Customer getCustomer() { return customer; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public RentalStatus getStatus() { return status; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public Integer getDaysCount() { return daysCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setCar(Car car) { this.car = car; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setStatus(RentalStatus status) { this.status = status; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setDaysCount(Integer daysCount) { this.daysCount = daysCount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
