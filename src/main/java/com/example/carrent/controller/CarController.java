package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import com.example.carrent.repository.CarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class CarController {

    private final CarRepository carRepo;

    public CarController(CarRepository carRepo) {
        this.carRepo = carRepo;
    }

    // ====== ОБЩИЙ КАТАЛОГ (REGULAR) ======
    @GetMapping("/cars")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String vehicleClass,
                       @RequestParam(required = false) String transmission,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sort,
                       Model model) {

        List<Car> cars = carRepo.findByCatalog(CatalogType.REGULAR); // <-- ключевое отличие

        String qq = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String vc = vehicleClass == null ? "" : vehicleClass.trim().toLowerCase(Locale.ROOT);
        String tr = transmission == null ? "" : transmission.trim().toLowerCase(Locale.ROOT);

        List<Car> filtered = cars.stream()
                .filter(c -> qq.isBlank() ||
                        (c.getMake() != null && c.getMake().toLowerCase(Locale.ROOT).contains(qq)) ||
                        (c.getModel() != null && c.getModel().toLowerCase(Locale.ROOT).contains(qq)))
                .filter(c -> vc.isBlank() ||
                        (c.getVehicleClass() != null && c.getVehicleClass().toLowerCase(Locale.ROOT).equals(vc)))
                .filter(c -> tr.isBlank() ||
                        (c.getTransmission() != null && c.getTransmission().toLowerCase(Locale.ROOT).equals(tr)))
                .filter(c -> maxPrice == null || (c.getDailyPrice() != null && c.getDailyPrice().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());

        if (sort != null) {
            switch (sort) {
                case "price_asc" -> filtered.sort(Comparator.comparing(
                        Car::getDailyPrice, Comparator.nullsLast(BigDecimal::compareTo)));
                case "price_desc" -> filtered.sort(Comparator.comparing(
                        Car::getDailyPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
                case "make" -> filtered.sort(Comparator.comparing(
                        c -> Optional.ofNullable(c.getMake()).orElse("")));
                case "newest" -> filtered.sort(Comparator.comparing(
                        Car::getYear, Comparator.nullsLast(Integer::compareTo)).reversed());
                default -> {}
            }
        }

        model.addAttribute("cars", filtered);
        model.addAttribute("q", q);
        model.addAttribute("vehicleClass", vehicleClass);
        model.addAttribute("transmission", transmission);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("mode", "regular");
        return "cars";
    }

    // ====== ТАКСИ (TAXI) ======
    @GetMapping("/cars/taxi")
    public String taxiCatalog(@RequestParam(required=false) String sort, Model model) {
        var cars = carRepo.findAll().stream()
                .filter(c -> c.getCatalog() == CatalogType.TAXI)
                .toList();
        // опционально применить сортировку по sort
        model.addAttribute("cars", cars);
        model.addAttribute("mode", "taxi");
        return "cars-taxi"; // у тебя отдельный шаблон — ок
    }

    @GetMapping("/cars/delivery")
    public String deliveryCatalog(@RequestParam(required=false) String sort, Model model) {
        var cars = carRepo.findAll().stream()
                .filter(c -> c.getCatalog() == CatalogType.DELIVERY)
                .toList();
        model.addAttribute("cars", cars);
        model.addAttribute("mode", "delivery");
        return "cars-delivery"; // отдельный шаблон — ок
    }

    private void applySort(List<Car> cars, String sort) {
        if (sort == null) return;
        switch (sort) {
            case "price_asc" -> cars.sort(Comparator.comparing(
                    Car::getDailyPrice, Comparator.nullsLast(BigDecimal::compareTo)));
            case "price_desc" -> cars.sort(Comparator.comparing(
                    Car::getDailyPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
            case "make" -> cars.sort(Comparator.comparing(
                    c -> Optional.ofNullable(c.getMake()).orElse("")));
            case "newest" -> cars.sort(Comparator.comparing(
                    Car::getYear, Comparator.nullsLast(Integer::compareTo)).reversed());
            default -> {}
        }
    }

    // ====== ДЕТАЛИ ======
    @GetMapping("/cars/{id}")
    public String details(@PathVariable Long id, Model model) {
        Car car = carRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("car", car);
        return "car-details";
    }
}
