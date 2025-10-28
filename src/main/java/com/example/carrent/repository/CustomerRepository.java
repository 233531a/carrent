package com.example.carrent.repository;

import com.example.carrent.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser_Id(Long userId);        // поиск по numeric id
    Optional<Customer> findByUser_Username(String username); // поиск по username
}

