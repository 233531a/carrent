package com.example.carrent.controller;

import com.example.carrent.model.Rental;
import com.example.carrent.model.RentalStatus;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Контроллер для панели менеджера.
 *
 * Предоставляет функции управления бронированиями:
 * - Просмотр всех бронирований с фильтрацией по статусу
 * - Поиск бронирований по клиенту, автомобилю или ID
 * - Одобрение заявок (PENDING → ACTIVE)
 * - Отклонение заявок (PENDING → REJECTED)
 * - Завершение аренд (ACTIVE → COMPLETED)
 *
 * Доступен пользователям с ролями MANAGER или ADMIN.
 *
 * @author Система аренды автомобилей
 */
@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ManagerController {

    private final RentalRepository rentalRepo;
    private final BookingService bookingService;

    public ManagerController(RentalRepository rentalRepo, BookingService bookingService) {
        this.rentalRepo = rentalRepo;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String root() {
        return "redirect:/manager/rentals";
    }

    @GetMapping("/rentals")
    public String rentals(@RequestParam Optional<String> status,
                          @RequestParam Optional<String> q,
                          Model model) {

        List<Rental> list;
        if (status.isPresent() && !status.get().isBlank() && !"ALL".equalsIgnoreCase(status.get())) {
            RentalStatus st = RentalStatus.valueOf(status.get().toUpperCase(Locale.ROOT));
            list = rentalRepo.findByStatusOrderByCreatedAtDesc(st);
        } else {
            list = rentalRepo.findAllByOrderByCreatedAtDesc();
        }

        String query = q.map(s -> s.trim().toLowerCase(Locale.ROOT)).orElse("");
        if (!query.isBlank()) {
            list = list.stream().filter(r -> {
                String user = r.getCustomer() != null && r.getCustomer().getUser() != null
                        ? r.getCustomer().getUser().getUsername() : "";
                String car = r.getCar() != null
                        ? (String.valueOf(r.getCar().getMake()) + " " + String.valueOf(r.getCar().getModel()))
                        : "";
                String id = String.valueOf(r.getId());
                return user.toLowerCase(Locale.ROOT).contains(query)
                        || car.toLowerCase(Locale.ROOT).contains(query)
                        || id.equals(query);
            }).sorted(Comparator.comparing(Rental::getCreatedAt).reversed()).toList();
        }

        model.addAttribute("title", "Заявки и аренды");
        model.addAttribute("rentals", list);
        model.addAttribute("status", status.orElse("ALL"));
        model.addAttribute("q", q.orElse(""));
        return "manager";
    }

    @PostMapping("/rentals/{id}/approve")
    public String approve(@PathVariable Long id) {
        bookingService.approve(id);
        return "redirect:/manager/rentals?status=ALL&ok=" + id;
    }

    @PostMapping("/rentals/{id}/reject")
    public String reject(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal) {
        bookingService.reject(id, principal.getUsername());
        return "redirect:/manager/rentals?status=ALL&rejected=" + id;
    }

    @PostMapping("/rentals/{id}/complete")
    public String complete(@PathVariable Long id) {
        bookingService.complete(id);
        return "redirect:/manager/rentals?status=ALL&completed=" + id;
    }
}
