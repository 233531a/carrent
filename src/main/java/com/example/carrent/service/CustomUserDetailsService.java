package com.example.carrent.service;

import com.example.carrent.model.User;
import com.example.carrent.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Сервис для загрузки данных пользователя для Spring Security.
 *
 * Реализует интерфейс UserDetailsService, который используется Spring Security
 * для аутентификации пользователей. Преобразует модель User в UserDetails,
 * необходимый для работы механизма безопасности.
 *
 * Процесс аутентификации:
 * 1. Пользователь вводит логин и пароль
 * 2. Spring Security вызывает loadUserByUsername()
 * 3. Сервис загружает пользователя из БД
 * 4. Spring Security сравнивает пароли и проверяет права доступа
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepo;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param userRepo репозиторий пользователей
     */
    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Загрузить пользователя по логину для Spring Security.
     *
     * Преобразует модель User в UserDetails, включая:
     * - Логин и зашифрованный пароль
     * - Статус активности (enabled)
     * - Список ролей (авторитеты)
     *
     * Все учетные записи считаются:
     * - Не истекшими (accountNonExpired = true)
     * - Не заблокированными (accountNonLocked = true)
     * - С не истекшими учетными данными (credentialsNonExpired = true)
     *
     * @param username логин пользователя
     * @return объект UserDetails для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toSet())
        );
    }
}
