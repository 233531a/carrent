package com.example.carrent.controller;

import com.example.carrent.model.Car;
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
                              Model model) {
        List<Car> cars = carRepo.findAll();
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
            List<Car> cars = carRepo.findAll();
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
