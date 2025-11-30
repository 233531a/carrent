package com.example.carrent.service;

import com.example.carrent.model.Customer;
import com.example.carrent.model.Role;
import com.example.carrent.model.User;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RoleRepository;
import com.example.carrent.repository.UserRepository;
import com.example.carrent.web.dto.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления пользователями.
 *
 * Предоставляет бизнес-логику для:
 * - Регистрации новых пользователей (клиентов)
 * - Создания сотрудников, менеджеров и администраторов
 * - Управления профилями клиентов
 *
 * @author Система аренды автомобилей
 */
@Service
public class UserService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepo;

    /**
     * Репозиторий для работы с ролями.
     */
    private final RoleRepository roleRepo;

    /**
     * Репозиторий для работы с профилями клиентов.
     */
    private final CustomerRepository customerRepo;

    /**
     * Кодировщик паролей (BCrypt).
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userRepo репозиторий пользователей
     * @param roleRepo репозиторий ролей
     * @param customerRepo репозиторий клиентов
     * @param passwordEncoder кодировщик паролей
     */
    public UserService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       CustomerRepository customerRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Зарегистрировать нового пользователя (клиента).
     *
     * Процесс регистрации:
     * 1. Проверка уникальности логина
     * 2. Проверка совпадения паролей
     * 3. Создание пользователя с зашифрованным паролем
     * 4. Назначение роли ROLE_CLIENT
     * 5. Создание профиля клиента
     *
     * @param form форма регистрации с данными пользователя
     * @throws IllegalArgumentException если логин уже занят или пароли не совпадают
     */
    @Transactional
    public void register(RegistrationForm form) {
        if (userRepo.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(true);

        Role clientRole = roleRepo.findByName(Role.ROLE_CLIENT)
                .orElseGet(() -> roleRepo.save(new Role(Role.ROLE_CLIENT)));
        user.addRole(clientRole);

        user = userRepo.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customerRepo.save(customer);
    }
}
