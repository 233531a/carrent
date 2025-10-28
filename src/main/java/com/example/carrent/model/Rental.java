package com.example.carrent.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связи
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Даты
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Статус (как строка)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20 /*, columnDefinition = "varchar(20)"*/)
    private RentalStatus status = RentalStatus.PENDING;

    // Денежные поля
    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay; // будет копией car.daily_price на момент брони

    @Column(name = "days_count", nullable = false)
    private Integer daysCount;

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // Таймстампы
    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public Rental() {}

    // ====== Логика автозаполнения ======
    @PrePersist
    public void prePersist() {
        validateDates();
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = this.createdAt;

        // если не проставлены — рассчитать
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

        // пересчёт при изменении дат/цены
        if (this.startDate != null && this.endDate != null) {
            long days = ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1;
            if (days <= 0) throw new IllegalArgumentException("Диапазон дат некорректен");
            this.daysCount = (int) days;
        }
        if (this.pricePerDay == null && this.car != null) {
            this.pricePerDay = this.car.getDailyPrice();
        }
        if (this.pricePerDay != null && this.daysCount != null) {
            this.totalAmount = this.pricePerDay.multiply(BigDecimal.valueOf(this.daysCount));
        }
    }

    private void validateDates() {
        if (this.startDate == null || this.endDate == null) {
            throw new IllegalArgumentException("Даты бронирования обязательны");
        }
        if (this.endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Дата окончания раньше даты начала");
        }
    }

    // ====== getters / setters ======
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
