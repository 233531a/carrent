package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.repository.CarRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
                       Model model) {

        List<Car> cars = carRepo.findAll();

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

        model.addAttribute("cars", filtered);
        model.addAttribute("q", q);
        model.addAttribute("vehicleClass", vehicleClass);
        model.addAttribute("transmission", transmission);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("catalogType", "common");
        return "cars";
    }

    // CarController.java (добавляем обратно спец-каталоги)
    @GetMapping("/cars/taxi")
    public String taxiCatalog(Model model) {
        model.addAttribute("cars", carRepo.findAll());
        model.addAttribute("catalogType", "taxi");
        return "cars";
    }

    @GetMapping("/cars/delivery")
    public String deliveryCatalog(Model model) {
        model.addAttribute("cars", carRepo.findAll());
        model.addAttribute("catalogType", "delivery");
        return "cars";
    }

}
