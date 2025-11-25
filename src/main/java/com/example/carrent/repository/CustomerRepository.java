package com.example.carrent.repository;

import com.example.carrent.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с профилями клиентов.
 *
 * Предоставляет методы для поиска профилей клиентов по связанному пользователю.
 * Наследует стандартные методы CRUD от JpaRepository.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Найти профиль клиента по идентификатору пользователя.
     *
     * @param userId ID пользователя
     * @return Optional с профилем клиента, если найден, иначе пустой Optional
     */
    Optional<Customer> findByUser_Id(Long userId);

    /**
     * Найти профиль клиента по логину пользователя.
     *
     * @param username логин пользователя
     * @return Optional с профилем клиента, если найден, иначе пустой Optional
     */
    Optional<Customer> findByUser_Username(String username);
}

