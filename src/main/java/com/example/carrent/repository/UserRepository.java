package com.example.carrent.repository;

import com.example.carrent.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 *
 * Предоставляет методы для поиска пользователей по логину.
 * Наследует стандартные методы CRUD от JpaRepository.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по логину.
     *
     * @param username логин пользователя
     * @return Optional с пользователем, если найден, иначе пустой Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверить существование пользователя с указанным логином.
     *
     * Используется при регистрации для проверки уникальности логина.
     *
     * @param username логин для проверки
     * @return true если пользователь с таким логином существует, false иначе
     */
    boolean existsByUsername(String username);
}
