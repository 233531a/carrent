package com.example.carrent.config;

import com.example.carrent.model.Customer;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerBootstrap {

    @Bean
    ApplicationRunner ensureCustomers(UserRepository users, CustomerRepository customers) {
        return args -> users.findAll().forEach(u -> {
            if (customers.findByUser_Id(u.getId()).isEmpty()) {
                var c = new Customer();
                c.setUser(u);
                c.setFullName(u.getUsername()); // можно потом отредактировать в ЛК
                customers.save(c);
            }
        });
    }
}
