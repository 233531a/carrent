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

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final CustomerRepository customerRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       CustomerRepository customerRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
    }

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

        // По умолчанию регистрируем как клиента
        Role clientRole = roleRepo.findByName(Role.ROLE_CLIENT)
                .orElseGet(() -> roleRepo.save(new Role(Role.ROLE_CLIENT)));
        user.addRole(clientRole);

        user = userRepo.save(user);

        // Создаём профиль клиента
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName(form.getUsername());
        customerRepo.save(customer);
    }

    /** Создание сотрудника (EMPLOYEE, MANAGER или ADMIN) вручную */
    @Transactional
    public User createEmployee(String username, String rawPassword, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);

        Role role = roleRepo.findByName(roleName)
                .orElseGet(() -> roleRepo.save(new Role(roleName)));
        user.addRole(role);

        return userRepo.save(user);
    }
}
