package com.example.carrent.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель пользователя системы.
 *
 * Представляет учетную запись пользователя для аутентификации и авторизации.
 * Содержит базовые данные для входа в систему: логин и зашифрованный пароль.
 *
 * Связи:
 * - ManyToMany с Role: пользователь может иметь несколько ролей
 * - OneToOne с Customer: у пользователя может быть профиль клиента
 *
 * Используется Spring Security для проверки прав доступа.
 *
 * @author Система аренды автомобилей
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя (уникальный).
     * Используется для входа в систему.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Зашифрованный пароль пользователя.
     * Хранится в виде BCrypt хеша, исходный пароль не восстанавливается.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Флаг активности пользователя.
     * false - пользователь заблокирован и не может войти в систему.
     * true - пользователь активен и может использовать систему.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Роли пользователя.
     * Связь ManyToMany с таблицей roles через промежуточную таблицу user_roles.
     * Загружается сразу (EAGER) для проверки прав доступа.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Конструктор по умолчанию (требуется JPA).
     */
    public User() {
    }

    // ========== GETTERS ==========

    /**
     * Получить идентификатор пользователя.
     *
     * @return уникальный ID пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Получить логин пользователя.
     *
     * @return логин пользователя
     */
    public String getUsername() {
        return username;
    }

    /**
     * Получить зашифрованный пароль.
     *
     * @return хеш пароля (BCrypt)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Проверить, активен ли пользователь.
     *
     * @return true если пользователь активен, false если заблокирован
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Получить набор ролей пользователя.
     *
     * @return множество ролей пользователя
     */
    public Set<Role> getRoles() {
        return roles;
    }

    // ========== SETTERS ==========

    /**
     * Установить идентификатор пользователя.
     *
     * @param id уникальный ID пользователя
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Установить логин пользователя.
     *
     * @param username логин пользователя (должен быть уникальным)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Установить зашифрованный пароль.
     *
     * @param password хеш пароля (BCrypt)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Установить статус активности пользователя.
     *
     * @param enabled true для активации, false для блокировки
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Установить роли пользователя.
     *
     * @param roles множество ролей пользователя
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Добавить роль пользователю.
     *
     * @param role роль для добавления
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Проверить, есть ли у пользователя роль с указанным именем.
     *
     * Поиск выполняется без учета регистра.
     *
     * @param roleName название роли (например, "ROLE_ADMIN")
     * @return true если пользователь имеет указанную роль, false иначе
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
    }
}
