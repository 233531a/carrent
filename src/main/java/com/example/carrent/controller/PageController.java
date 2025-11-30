package com.example.carrent.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для информационных страниц приложения.
 *
 * Обрабатывает статические страницы, не требующие сложной бизнес-логики.
 * Маршруты для каталогов автомобилей (/cars/taxi, /cars/delivery) обрабатываются
 * в CarController, чтобы избежать дублирования.
 *
 * @author Система аренды автомобилей
 */
@Controller
public class PageController {

    /**
     * Отображает страницу "О нас".
     *
     * GET /about
     *
     * @return имя шаблона "about" (templates/about.html)
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /**
     * Обработчик запросов favicon.ico.
     *
     * Браузеры автоматически запрашивают favicon.ico для каждой страницы.
     * Этот метод предотвращает исключение NoResourceFoundException,
     * возвращая пустой ответ со статусом 204 No Content.
     *
     * GET /favicon.ico
     *
     * @return ResponseEntity с пустым телом и статусом 204
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}