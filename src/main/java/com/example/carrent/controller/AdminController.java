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

/**
 * Контроллер административной панели.
 *
 * Предоставляет главную страницу администратора с общей статистикой системы.
 * Доступен только пользователям с ролью ADMIN.
 *
 * @author Система аренды автомобилей
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * Репозиторий для работы с автомобилями.
     */
    private final CarRepository carRepo;

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepo;

    /**
     * Репозиторий для работы с арендами.
     */
    private final RentalRepository rentalRepo;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param carRepo репозиторий автомобилей
     * @param userRepo репозиторий пользователей
     * @param rentalRepo репозиторий аренд
     */
    public AdminController(CarRepository carRepo, UserRepository userRepo, RentalRepository rentalRepo) {
        this.carRepo = carRepo;
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
    }

    /**
     * Отобразить административную панель.
     *
     * GET /admin
     *
     * Загружает статистику системы:
     * - Общее количество автомобилей
     * - Общее количество пользователей
     * - Общее количество аренд
     * - Последние 10 добавленных автомобилей
     * - Последние 10 созданных аренд
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "admin" (templates/admin.html)
     */
    @GetMapping("/admin")
    public String admin(Model model) {
        long carsCount = carRepo.count();
        long usersCount = userRepo.count();
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

        return "admin";
    }
}
