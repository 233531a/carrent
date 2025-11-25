package com.example.carrent.model;

import jakarta.persistence.*;

/**
 * Модель профиля клиента.
 *
 * Расширяет базовую информацию пользователя (User) дополнительными данными:
 * полное имя (ФИО).
 *
 * Связь с User: OneToOne (один пользователь = один профиль клиента).
 * Не все пользователи имеют профиль клиента (например, сотрудники могут не иметь).
 *
 * Используется для:
 * - Отображения информации в личном кабинете
 * - Связи с арендами (Rental)
 * - Идентификации клиента при бронировании
 *
 * @author Система аренды автомобилей
 * @version 1.1
 * @since 2025-01-25
 */
@Entity
@Table(name = "customers")
public class Customer {

    /**
     * Уникальный идентификатор профиля клиента.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с пользователем (OneToOne).
     * Каждый профиль клиента привязан к одному пользователю.
     * Загружается лениво (LAZY) для оптимизации запросов.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ========== GETTERS ==========

    /**
     * Получить идентификатор профиля клиента.
     *
     * @return уникальный ID профиля
     */
    public Long getId() {
        return id;
    }


    /**
     * Получить связанного пользователя.
     *
     * @return объект User, к которому привязан этот профиль
     */
    public User getUser() {
        return user;
    }

    // ========== SETTERS ==========

    /**
     * Установить идентификатор профиля клиента.
     *
     * @param id уникальный ID профиля
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Установить связанного пользователя.
     *
     * @param user объект User, к которому привязывается профиль
     */
    public void setUser(User user) {
        this.user = user;
    }
}
