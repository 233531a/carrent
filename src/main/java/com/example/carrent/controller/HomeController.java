package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.repository.CarRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CarRepository carRepository;

    public HomeController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        long fleetCount = carRepository.count();

        var pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Car> carsTop = carRepository
                .findByCatalog(com.example.carrent.model.CatalogType.REGULAR, pageable)
                .getContent();

        model.addAttribute("fleetCount", fleetCount);
        model.addAttribute("carsTop", carsTop);
        return "index";
    }
}
