package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import com.example.carrent.repository.CarRepository;
import com.example.carrent.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final CarRepository carRepo;
    private final BookingService bookingService;

    public BookingController(CarRepository carRepo, BookingService bookingService) {
        this.carRepo = carRepo;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String bookingPage(@RequestParam(value = "carId", required = false) Long carId,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        List<Car> cars;
        
        // Check if user has only CLIENT role
        boolean isClientOnly = principal != null && 
            principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")) &&
            principal.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") || 
                               a.getAuthority().equals("ROLE_MANAGER") || 
                               a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isClientOnly) {
            // Clients can only book REGULAR cars
            cars = carRepo.findAll().stream()
                    .filter(c -> c.getCatalog() == CatalogType.REGULAR)
                    .collect(Collectors.toList());
        } else {
            // Others (employees, managers, admins) can book REGULAR, TAXI, or DELIVERY
            cars = carRepo.findAll();
        }
        
        model.addAttribute("cars", cars);
        model.addAttribute("selectedCarId", carId);
        return "booking";
    }

    @PostMapping
    public String makeBooking(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam Long carId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {

        // server-side защита на случай отключённого JS
        LocalDate today = LocalDate.now();
        if (startDate == null || endDate == null
                || startDate.isBefore(today)
                || endDate.isBefore(startDate)) {
            List<Car> cars;
            
            // Apply same filtering as in bookingPage
            boolean isClientOnly = principal != null && 
                principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")) &&
                principal.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") || 
                                   a.getAuthority().equals("ROLE_MANAGER") || 
                                   a.getAuthority().equals("ROLE_ADMIN"));
            
            if (isClientOnly) {
                cars = carRepo.findAll().stream()
                        .filter(c -> c.getCatalog() == CatalogType.REGULAR)
                        .collect(Collectors.toList());
            } else {
                cars = carRepo.findAll();
            }
            
            model.addAttribute("cars", cars);
            model.addAttribute("selectedCarId", carId);
            String err = (startDate == null || endDate == null)
                    ? "Заполните обе даты."
                    : startDate.isBefore(today)
                    ? "Дата начала не может быть в прошлом."
                    : "Дата окончания должна быть позже даты начала.";
            model.addAttribute("dateError", err);
            return "booking";
        }

        bookingService.book(principal.getUsername(), carId, startDate, endDate);
        return "redirect:/account";
    }
}
