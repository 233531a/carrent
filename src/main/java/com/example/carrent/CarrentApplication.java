package com.example.carrent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения для системы аренды автомобилей.
 *
 * Точка входа в приложение. При запуске:
 * - Инициализирует Spring контекст
 * - Загружает все компоненты (контроллеры, сервисы, репозитории)
 * - Настраивает подключение к базе данных
 * - Запускает встроенный сервер (Tomcat) на порту 8080
 *
 * @author Система аренды автомобилей
 */
@SpringBootApplication
public class CarrentApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(CarrentApplication.class, args);
        System.out.println("http://localhost:8080");
    }
}
