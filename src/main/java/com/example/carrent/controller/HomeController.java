package com.example.carrent.controller;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import com.example.carrent.repository.CarRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Контроллер главной страницы приложения.
 *
 * Обрабатывает запросы к корневому URL и отображает главную страницу
 * с информацией о количестве автомобилей и топовыми автомобилями.
 *
 * @author Система аренды автомобилей
 */
@Controller
public class HomeController {

    /**
     * Репозиторий для работы с автомобилями.
     */
    private final CarRepository carRepository;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param carRepository репозиторий автомобилей
     */
    public HomeController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Отобразить главную страницу приложения.
     *
     * GET /
     *
     * Загружает:
     * - Общее количество автомобилей в парке
     * - Топ-6 последних добавленных автомобилей из обычного каталога
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "index" (templates/index.html)
     */
    @GetMapping("/")
    public String home(Model model) {
        long fleetCount = carRepository.count();

        var pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Car> carsTop = carRepository
                .findByCatalog(CatalogType.REGULAR, pageable)
                .getContent();

        model.addAttribute("fleetCount", fleetCount);
        model.addAttribute("carsTop", carsTop);
        return "index";
    }
}
