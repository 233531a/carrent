package com.example.carrent.controller;

import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import com.example.carrent.service.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер личного кабинета пользователя.
 *
 * Отвечает за:
 * - Отображение профиля клиента
 * - Просмотр истории бронирований
 * - Отмену активных броней
 * - Завершение аренды клиентом
 *
 * Доступ: только авторизованные пользователи (настроено в SecurityConfig).
 *
 * @author Система аренды автомобилей
 * @version 1.1
 * @since 2025-11-25
 */
@Controller
public class AccountController {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final RentalRepository rentalRepo;
    private final BookingService bookingService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userRepo репозиторий пользователей
     * @param customerRepo репозиторий клиентов
     * @param rentalRepo репозиторий аренд
     * @param bookingService сервис управления бронированиями
     */
    public AccountController(UserRepository userRepo,
                             CustomerRepository customerRepo,
                             RentalRepository rentalRepo,
                             BookingService bookingService) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.rentalRepo = rentalRepo;
        this.bookingService = bookingService;
    }

    /**
     * Отобразить личный кабинет клиента.
     *
     * GET /account
     *
     * Загружает:
     * - Профиль клиента (полное имя)
     * - Историю всех бронирований (отсортированных по дате создания)
     *
     * @param me текущий авторизованный пользователь (автоматически внедряется Spring Security)
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона "account" (templates/account.html)
     */
    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails me, Model model) {
        var user = userRepo.findByUsername(me.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        var customer = customerRepo.findByUser_Id(user.getId()).orElse(null);
        model.addAttribute("customer", customer);

        if (customer != null) {
            var rentals = rentalRepo.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
            model.addAttribute("rentals", rentals);
        }

        return "account";
    }

    /**
     * Отменить бронирование клиентом.
     *
     * POST /account/rentals/{id}/cancel
     *
     * Клиент может отменить только свою бронь.
     * После отмены:
     * - Статус меняется на CANCELLED
     * - Машина может стать доступной (если нет других броней)
     * - Клиент перенаправляется обратно в личный кабинет с сообщением
     *
     * @param me текущий авторизованный пользователь
     * @param id ID бронирования для отмены
     * @return редирект на /account с параметром cancelled={id}
     */
    @PostMapping("/account/rentals/{id}/cancel")
    public String cancel(@AuthenticationPrincipal UserDetails me, @PathVariable Long id) {
        bookingService.cancel(id, me.getUsername());
        return "redirect:/account?cancelled=" + id;
    }

    /**
     * Завершить аренду клиентом.
     *
     * POST /account/rentals/{id}/complete
     *
     * Клиент отмечает, что использование автомобиля завершено.
     * После завершения:
     * - Статус меняется на COMPLETED
     * - Машина может стать доступной (если нет других броней)
     * - Клиент перенаправляется обратно в личный кабинет с сообщением
     *
     * @param me текущий авторизованный пользователь
     * @param id ID бронирования для завершения
     * @return редирект на /account с параметром completed={id}
     */
    @PostMapping("/account/rentals/{id}/complete")
    public String complete(@AuthenticationPrincipal UserDetails me, @PathVariable Long id) {
        bookingService.completeByCustomer(me.getUsername(), id);
        return "redirect:/account?completed=" + id;
    }
}
