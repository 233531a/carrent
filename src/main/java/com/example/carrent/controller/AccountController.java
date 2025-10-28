package com.example.carrent.controller;

import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import com.example.carrent.service.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final RentalRepository rentalRepo;
    private final BookingService bookingService;

    public AccountController(UserRepository userRepo,
                             CustomerRepository customerRepo,
                             RentalRepository rentalRepo,
                             BookingService bookingService) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.rentalRepo = rentalRepo;
        this.bookingService = bookingService;
    }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails me, Model model) {
        var user = userRepo.findByUsername(me.getUsername()).orElseThrow();
        var customer = customerRepo.findByUser_Id(user.getId()).orElse(null);
        model.addAttribute("customer", customer);

        if (customer != null) {
            var rentals = rentalRepo.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
            model.addAttribute("rentals", rentals);
        }
        return "account";
    }

    @PostMapping("/account/rentals/{id}/cancel")
    public String cancel(@AuthenticationPrincipal UserDetails me, @PathVariable Long id) {
        bookingService.cancel(me.getUsername(), id);
        return "redirect:/account?cancelled=" + id;
    }
}
