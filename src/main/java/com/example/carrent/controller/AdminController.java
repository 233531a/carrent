package com.example.carrent.controller;

import com.example.carrent.repository.CarRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CarRepository carRepo;
    private final UserRepository userRepo;
    private final RentalRepository rentalRepo;

    public AdminController(CarRepository carRepo, UserRepository userRepo, RentalRepository rentalRepo) {
        this.carRepo = carRepo;
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        long carsCount    = carRepo.count();
        long usersCount   = userRepo.count();
        long rentalsCount = rentalRepo.count();

        var cars = carRepo.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))).getContent();
        var recentRentals = rentalRepo.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        model.addAttribute("carsCount", carsCount);
        model.addAttribute("usersCount", usersCount);
        model.addAttribute("rentalsCount", rentalsCount);
        model.addAttribute("cars", cars);
        model.addAttribute("recentRentals", recentRentals);

        return "admin"; // templates/admin.html
    }
}
