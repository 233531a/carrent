package com.example.carrent.service;

import com.example.carrent.model.User;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final RentalRepository rentalRepo;

    public AdminUserService(UserRepository userRepo,
                            CustomerRepository customerRepo,
                            RentalRepository rentalRepo) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.rentalRepo = rentalRepo;
    }

    @Transactional
    public void deleteUserHard(Long userId) {
        User u = userRepo.findById(userId).orElseThrow();

        var customer = customerRepo.findByUser_Id(userId).orElse(null);
        if (customer != null) {
            // 1) удаляем все аренды этого клиента
            rentalRepo.deleteByCustomer_Id(customer.getId());
            // 2) удаляем самого клиента
            customerRepo.delete(customer);
        }
        // 3) удаляем пользователя
        userRepo.delete(u);
    }
}
