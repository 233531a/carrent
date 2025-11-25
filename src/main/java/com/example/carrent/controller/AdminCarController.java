package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.repository.CarRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Контроллер для управления автомобилями администратором.
 *
 * Предоставляет CRUD операции для автомобилей:
 * - Просмотр списка всех автомобилей
 * - Создание нового автомобиля
 * - Редактирование существующего автомобиля
 * - Удаление автомобиля
 *
 * Доступен только пользователям с ролью ADMIN.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
@Controller
@RequestMapping("/admin/cars")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCarController {

    private final CarRepository carRepo;

    public AdminCarController(CarRepository carRepo) {
        this.carRepo = carRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cars", carRepo.findAll());
        return "admin-cars";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Car car = new Car();
        car.setAvailable(true);
        model.addAttribute("car", car);
        model.addAttribute("titleForm", "Добавить автомобиль");
        return "admin-car-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("car") Car car, BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titleForm", "Добавить автомобиль");
            return "admin-car-form";
        }
        if (car.getDailyPrice() == null) car.setDailyPrice(BigDecimal.ZERO);
        carRepo.save(car);
        return "redirect:/admin/cars?created";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Car car = carRepo.findById(id).orElseThrow();
        model.addAttribute("car", car);
        model.addAttribute("titleForm", "Редактировать автомобиль");
        return "admin-car-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("car") Car form,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("titleForm", "Редактировать автомобиль");
            return "admin-car-form";
        }
        Car car = carRepo.findById(id).orElseThrow();
        car.setMake(form.getMake());
        car.setModel(form.getModel());
        car.setYear(form.getYear());
        car.setVehicleClass(form.getVehicleClass());
        car.setTransmission(form.getTransmission());
        car.setAvailable(form.isAvailable());
        car.setDailyPrice(form.getDailyPrice() == null ? BigDecimal.ZERO : form.getDailyPrice());
        car.setCatalog(form.getCatalog());
        car.setPhotoUrl(form.getPhotoUrl());


        carRepo.save(car);
        return "redirect:/admin/cars?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        carRepo.deleteById(id);
        return "redirect:/admin/cars?deleted";
    }
}
