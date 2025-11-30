package com.example.carrent.model;

import jakarta.persistence.*;

/**
 * Модель роли пользователя в системе.
 *
 * Роли определяют права доступа пользователей к различным функциям приложения.
 * Один пользователь может иметь несколько ролей (связь ManyToMany с User).
 *
 * Доступные роли:
 * - ROLE_CLIENT: обычный клиент, может бронировать автомобили
 * - ROLE_EMPLOYEE: сотрудник, имеет доступ к специальным каталогам
 * - ROLE_MANAGER: менеджер, может одобрять/отклонять бронирования
 * - ROLE_ADMIN: администратор, полный доступ ко всем функциям
 *
 * @author Система аренды автомобилей
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
)
public class Role {

    /**
     * Константа для роли клиента.
     * Используется в коде для проверки прав доступа.
     */
    public static final String ROLE_CLIENT = "ROLE_CLIENT";

    /**
     * Константа для роли сотрудника.
     * Используется в коде для проверки прав доступа.
     */
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

    /**
     * Константа для роли менеджера.
     * Используется в коде для проверки прав доступа.
     */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";

    /**
     * Константа для роли администратора.
     * Используется в коде для проверки прав доступа.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Уникальный идентификатор роли.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название роли (например, "ROLE_CLIENT", "ROLE_ADMIN").
     * Должно соответствовать константам выше.
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Конструктор по умолчанию (требуется JPA).
     */
    public Role() {
    }

    /**
     * Конструктор с названием роли.
     *
     * @param name название роли (например, "ROLE_CLIENT")
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Получить идентификатор роли.
     *
     * @return уникальный ID роли
     */
    public Long getId() {
        return id;
    }

    /**
     * Установить идентификатор роли.
     *
     * @param id уникальный ID роли
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Получить название роли.
     *
     * @return название роли (например, "ROLE_CLIENT")
     */
    public String getName() {
        return name;
    }

    /**
     * Установить название роли.
     *
     * @param name название роли (например, "ROLE_CLIENT")
     */
    public void setName(String name) {
        this.name = name;
    }
}
