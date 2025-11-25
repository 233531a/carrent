package com.example.carrent.repository;

import com.example.carrent.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями пользователей.
 *
 * Предоставляет методы для поиска ролей по названию.
 * Наследует стандартные методы CRUD от JpaRepository.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Найти роль по названию.
     *
     * @param name название роли (например, "ROLE_CLIENT", "ROLE_ADMIN")
     * @return Optional с ролью, если найдена, иначе пустой Optional
     */
    Optional<Role> findByName(String name);
}
