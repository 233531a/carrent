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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с каталогами автомобилей.
 *
 * Обрабатывает запросы к различным каталогам:
 * - /cars - обычный каталог (REGULAR), доступен всем
 * - /cars/taxi - каталог такси (TAXI), только для сотрудников
 * - /cars/delivery - каталог доставки (DELIVERY), только для сотрудников
 * - /cars/{id} - детали конкретного автомобиля
 *
 * Поддерживает фильтрацию и сортировку автомобилей.
 *
 * @author Система аренды автомобилей
 */
@Controller
public class CarController {

    private final CarRepository carRepo;

    public CarController(CarRepository carRepo) {
        this.carRepo = carRepo;
    }

    @GetMapping("/cars")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String vehicleClass,
                       @RequestParam(required = false) String transmission,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sort,
                       Model model) {

        List<Car> cars = carRepo.findByCatalog(CatalogType.REGULAR);

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

    @GetMapping("/cars/taxi")
    public String taxiCatalog(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String vehicleClass,
                              @RequestParam(required = false) String transmission,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              @RequestParam(required = false) String sort,
                              Model model) {

        List<Car> cars = carRepo.findByCatalog(CatalogType.TAXI);

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
        model.addAttribute("mode", "taxi");
        return "cars-taxi";
    }

    @GetMapping("/cars/delivery")
    public String deliveryCatalog(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) String vehicleClass,
                                  @RequestParam(required = false) String transmission,
                                  @RequestParam(required = false) BigDecimal maxPrice,
                                  @RequestParam(required = false) String sort,
                                  Model model) {

        List<Car> cars = carRepo.findByCatalog(CatalogType.DELIVERY);

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
        model.addAttribute("mode", "delivery");
        return "cars-delivery";
    }


    @GetMapping("/cars/{id}")
    public String details(@PathVariable Long id, Model model) {
        Car car = carRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("car", car);
        return "car-details";
    }
}
