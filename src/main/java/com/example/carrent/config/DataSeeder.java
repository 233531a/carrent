package com.example.carrent.config;

import com.example.carrent.model.Role;
import com.example.carrent.model.User;
import com.example.carrent.repository.RoleRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DataSeeder(RoleRepository roles, UserRepository users, PasswordEncoder encoder) {
        this.roles = roles;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // создаём все роли, если нет
        for (String r : new String[]{
                Role.ROLE_CLIENT,
                Role.ROLE_EMPLOYEE,
                Role.ROLE_MANAGER,
                Role.ROLE_ADMIN
        }) {
            roles.findByName(r).orElseGet(() -> roles.save(new Role(r)));
        }

        // тестовый админ
        if (!users.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin"));
            admin.setEnabled(true);
            admin.getRoles().add(roles.findByName(Role.ROLE_ADMIN).get());
            users.save(admin);
        }

        // тестовый менеджер
        if (!users.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(encoder.encode("manager"));
            manager.setEnabled(true);
            manager.getRoles().add(roles.findByName(Role.ROLE_MANAGER).get());
            users.save(manager);
        }

        // тестовый сотрудник
        if (!users.existsByUsername("employee")) {
            User emp = new User();
            emp.setUsername("employee");
            emp.setPassword(encoder.encode("employee"));
            emp.setEnabled(true);
            emp.getRoles().add(roles.findByName(Role.ROLE_EMPLOYEE).get());
            users.save(emp);
        }

        // тестовый клиент
        if (!users.existsByUsername("client")) {
            User client = new User();
            client.setUsername("client");
            client.setPassword(encoder.encode("client"));
            client.setEnabled(true);
            client.getRoles().add(roles.findByName(Role.ROLE_CLIENT).get());
            users.save(client);
        }
    }
}
