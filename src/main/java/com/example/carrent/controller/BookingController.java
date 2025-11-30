package com.example.carrent.controller;

import com.example.carrent.api.dto.CarDTO;
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

/**
 * Контроллер для бронирования автомобилей.
 *
 * Обрабатывает:
 * - Отображение страницы бронирования
 * - Создание нового бронирования
 *
 * Доступен только авторизованным пользователям.
 * Клиенты могут бронировать только автомобили из каталога REGULAR.
 * Сотрудники, менеджеры и администраторы могут бронировать все типы автомобилей.
 *
 * @author Система аренды автомобилей
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final CarRepository carRepo;
    private final BookingService bookingService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param carRepo репозиторий автомобилей
     * @param bookingService сервис управления бронированиями
     */
    public BookingController(CarRepository carRepo, BookingService bookingService) {
        this.carRepo = carRepo;
        this.bookingService = bookingService;
    }

    /**
     * Проверить, является ли пользователь только клиентом (без других ролей).
     *
     * @param principal текущий авторизованный пользователь
     * @return true если пользователь имеет только роль ROLE_CLIENT, false иначе
     */
    private boolean isClientOnly(UserDetails principal) {
        if (principal == null) {
            return false;
        }
        boolean hasClientRole = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        boolean hasOtherRoles = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") ||
                        a.getAuthority().equals("ROLE_MANAGER") ||
                        a.getAuthority().equals("ROLE_ADMIN"));
        return hasClientRole && !hasOtherRoles;
    }

    /**
     * Получить список автомобилей для бронирования в зависимости от роли пользователя.
     *
     * @param principal текущий авторизованный пользователь
     * @return список доступных автомобилей для бронирования
     */
    private List<Car> getAvailableCarsForBooking(UserDetails principal) {
        if (isClientOnly(principal)) {
            return carRepo.findAll().stream()
                    .filter(c -> c.getCatalog() == CatalogType.REGULAR)
                    .collect(Collectors.toList());
        } else {
            return carRepo.findAll();
        }
    }

    /**
     * Получить список автомобилей, доступных для бронирования на указанные даты.
     *
     * Фильтрует автомобили по:
     * - Роли пользователя (клиенты видят только REGULAR)
     * - Доступности на указанные даты (исключает уже забронированные)
     *
     * @param principal текущий авторизованный пользователь
     * @param startDate дата начала аренды (может быть null)
     * @param endDate дата окончания аренды (может быть null)
     * @return список доступных автомобилей
     */
    private List<Car> getAvailableCarsForDates(UserDetails principal, LocalDate startDate, LocalDate endDate) {
        List<Car> cars = getAvailableCarsForBooking(principal);
        
        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            return cars.stream()
                    .filter(car -> bookingService.isCarAvailableForDates(car.getId(), startDate, endDate))
                    .collect(Collectors.toList());
        }
        
        return cars;
    }

    /**
     * Отобразить страницу бронирования.
     *
     * GET /booking
     *
     * Загружает список доступных автомобилей для бронирования.
     * Для клиентов показываются только автомобили из каталога REGULAR.
     * Для сотрудников, менеджеров и администраторов - все автомобили.
     *
     * @param carId опциональный ID автомобиля для предварительного выбора
     * @param startDate опциональная дата начала для фильтрации доступных автомобилей
     * @param endDate опциональная дата окончания для фильтрации доступных автомобилей
     * @param principal текущий авторизованный пользователь
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "booking" (templates/booking.html)
     */
    @GetMapping
    public String bookingPage(@RequestParam(value = "carId", required = false) Long carId,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        List<Car> cars = getAvailableCarsForDates(principal, startDate, endDate);
        
        model.addAttribute("cars", cars);
        model.addAttribute("selectedCarId", carId);
        if (startDate != null) {
            model.addAttribute("startDate", startDate);
        }
        if (endDate != null) {
            model.addAttribute("endDate", endDate);
        }
        return "booking";
    }

    /**
     * Создать новое бронирование.
     *
     * POST /booking
     *
     * Процесс:
     * 1. Валидация дат (не в прошлом, конец после начала)
     * 2. Проверка доступности автомобиля на указанные даты
     * 3. Создание бронирования через BookingService
     * 4. Редирект в личный кабинет при успехе
     *
     * При ошибке валидации возвращается на страницу бронирования с сообщением об ошибке.
     *
     * @param principal текущий авторизованный пользователь
     * @param carId ID автомобиля для бронирования
     * @param startDate дата начала аренды
     * @param endDate дата окончания аренды
     * @param model модель для передачи данных в шаблон
     * @return редирект на /account при успехе, имя шаблона "booking" при ошибке
     */
    @PostMapping
    public String makeBooking(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam Long carId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        LocalDate today = LocalDate.now();
        if (startDate == null || endDate == null
                || startDate.isBefore(today)
                || endDate.isBefore(startDate)) {
            List<Car> cars = getAvailableCarsForDates(principal, startDate, endDate);
            
            model.addAttribute("cars", cars);
            model.addAttribute("selectedCarId", carId);
            if (startDate != null) {
                model.addAttribute("startDate", startDate);
            }
            if (endDate != null) {
                model.addAttribute("endDate", endDate);
            }
            String err = (startDate == null || endDate == null)
                    ? "Заполните обе даты."
                    : startDate.isBefore(today)
                    ? "Дата начала не может быть в прошлом."
                    : "Дата окончания должна быть позже даты начала.";
            model.addAttribute("dateError", err);
            return "booking";
        }

        try {
            bookingService.book(carId, principal.getUsername(), startDate, endDate);
            return "redirect:/account?booked=" + carId;
        } catch (IllegalArgumentException ex) {
            List<Car> cars = getAvailableCarsForDates(principal, startDate, endDate);
            
            model.addAttribute("cars", cars);
            model.addAttribute("selectedCarId", carId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("dateError", ex.getMessage());
            return "booking";
        } catch (RuntimeException ex) {
            List<Car> cars = getAvailableCarsForDates(principal, startDate, endDate);
            
            model.addAttribute("cars", cars);
            model.addAttribute("selectedCarId", carId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("dateError", "Ошибка при создании бронирования: " + ex.getMessage());
            return "booking";
        }
    }

    /**
     * REST endpoint для проверки доступности автомобилей на указанные даты.
     *
     * GET /booking/api/available-cars?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     *
     * Используется JavaScript для динамического обновления списка автомобилей
     * при изменении дат на странице бронирования.
     *
     * @param startDate дата начала аренды
     * @param endDate дата окончания аренды
     * @param principal текущий авторизованный пользователь
     * @return список доступных автомобилей в формате JSON
     */
    @GetMapping("/api/available-cars")
    @ResponseBody
    public List<CarDTO> getAvailableCars(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails principal) {
        List<Car> cars = getAvailableCarsForDates(principal, startDate, endDate);
        return cars.stream()
                .map(car -> {
                    CarDTO dto = new CarDTO();
                    dto.setId(car.getId());
                    dto.setBrand(car.getMake());
                    dto.setModel(car.getModel());
                    dto.setYear(car.getYear());
                    dto.setPricePerDay(car.getDailyPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
