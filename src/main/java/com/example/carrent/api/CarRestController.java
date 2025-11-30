package com.example.carrent.api;

import com.example.carrent.api.dto.CarDTO;
import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import com.example.carrent.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API контроллер для управления автомобилями.
 *
 * Предоставляет JSON API для внешних клиентов:
 * - Мобильные приложения
 * - Сторонние сервисы
 * - Frontend-фреймворки (React, Vue, Angular)
 * - Внешние интеграции
 *
 * Все endpoints начинаются с /api/cars.
 * Правила доступа настроены в SecurityConfig:
 * - GET - публичный доступ (даже без авторизации)
 * - POST, PUT - только MANAGER или ADMIN
 * - DELETE - только ADMIN
 * - PATCH - только MANAGER или ADMIN
 *
 * Использует DTO (Data Transfer Object) паттерн:
 * - CarDTO для передачи данных клиентам
 * - Car (Entity) для работы с БД
 * - Преобразование происходит в методах convertToDTO/convertToEntity
 *
 * Формат ответов:
 * - 200 OK - успешная операция
 * - 201 Created - ресурс создан
 * - 400 Bad Request - невалидные данные
 * - 403 Forbidden - недостаточно прав
 * - 404 Not Found - ресурс не найден
 * - 500 Internal Server Error - ошибка сервера
 *
 * @author Система аренды автомобилей
 */
@RestController
@RequestMapping("/api/cars")
public class CarRestController {

    private final CarService carService;

    @Autowired
    public CarRestController(CarService carService) {
        this.carService = carService;
    }

    /**
     * Получить список всех автомобилей.
     *
     * GET /api/cars
     *
     * Доступ: публичный (без авторизации)
     *
     * Возвращает массив JSON объектов CarDTO.
     * Используйте параметры фильтрации в методе search() для более точных запросов.
     *
 * Пример ответа:
 * [
 *   {
 *     "id": 1,
 *     "brand": "Toyota",
 *     "model": "Camry",
 *     "pricePerDay": 5000.00,
 *     "vehicleClass": "business",
 *     "transmission": "AT",
 *     "year": YYYY,
 *     "available": true,
 *     "photoUrl": "/images/camry.jpg",
 *     "catalog": "REGULAR"
 *   },
 *   ...
 * ]
     *
     * @return ResponseEntity со списком всех автомобилей в формате CarDTO
     */
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> cars = carService.findAll();
        List<CarDTO> carDTOs = cars.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(carDTOs);
    }

    /**
     * Получить автомобиль по ID.
     *
     * GET /api/cars/{id}
     *
     * Доступ: публичный
     *
     * @param id ID автомобиля
     * @return ResponseEntity с CarDTO или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return carService.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новый автомобиль.
     *
     * POST /api/cars
     *
     * Доступ: только MANAGER или ADMIN (проверка в SecurityConfig)
     *
     * Тело запроса должно содержать JSON с данными автомобиля.
     * Поля с @NotBlank, @Min, @Max и другими аннотациями валидируются автоматически.
     *
 * Пример тела запроса:
 * {
 *   "brand": "Toyota",
 *   "model": "Camry",
 *   "pricePerDay": 5000.00,
 *   "vehicleClass": "business",
 *   "transmission": "AT",
 *   "year": YYYY,
 *   "available": true,
 *   "photoUrl": "/images/camry.jpg",
 *   "catalog": "REGULAR"
 * }
     *
     * @param carDTO данные нового автомобиля (валидируются через @Valid)
     * @return ResponseEntity с созданным CarDTO и статус 201 Created
     */
    @PostMapping
    public ResponseEntity<CarDTO> createCar(@Valid @RequestBody CarDTO carDTO) {
        Car car = convertToEntity(carDTO);
        Car savedCar = carService.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedCar));
    }

    /**
     * Обновить существующий автомобиль.
     *
     * PUT /api/cars/{id}
     *
     * Доступ: только MANAGER или ADMIN
     *
     * Тело запроса должно содержать полные данные автомобиля (все поля).
     * ID в URL должен совпадать с ID существующего автомобиля.
     *
     * @param id ID автомобиля для обновления
     * @param carDTO новые данные автомобиля
     * @return ResponseEntity с обновленным CarDTO или 404 если не найден
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarDTO> updateCar(@PathVariable Long id,
                                            @Valid @RequestBody CarDTO carDTO) {
        return carService.findById(id)
                .map(existingCar -> {
                    updateCarFromDTO(existingCar, carDTO);
                    Car updatedCar = carService.save(existingCar);
                    return ResponseEntity.ok(convertToDTO(updatedCar));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удалить автомобиль.
     *
     * DELETE /api/cars/{id}
     *
     * Доступ: только ADMIN (критическая операция)
     *
     * ВНИМАНИЕ: Перед удалением рекомендуется проверить, нет ли активных
     * бронирований на этот автомобиль. Иначе возможны ошибки FK.
     *
     * @param id ID автомобиля для удаления
     * @return ResponseEntity с кодом 204 No Content при успехе или 404 если не найден
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (carService.findById(id).isPresent()) {
            carService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Получить автомобили по типу каталога.
     *
     * GET /api/cars/catalog/{catalogType}
     *
     * Доступ: публичный
     *
     * Типы каталога:
     * - REGULAR - обычный каталог (доступен всем)
     * - TAXI - каталог такси (для сотрудников)
     * - DELIVERY - каталог доставки (для сотрудников)
     *
     * @param catalogType тип каталога (REGULAR, TAXI, DELIVERY)
     * @return ResponseEntity со списком автомобилей или 400 при невалидном типе
     */
    @GetMapping("/catalog/{catalogType}")
    public ResponseEntity<List<CarDTO>> getCarsByCatalog(@PathVariable String catalogType) {
        try {
            CatalogType catalog = CatalogType.valueOf(catalogType.toUpperCase());
            List<Car> cars = carService.findByCatalog(catalog);
            List<CarDTO> carDTOs = cars.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(carDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Изменить доступность автомобиля.
     *
     * PATCH /api/cars/{id}/availability?available=true
     *
     * Доступ: только MANAGER или ADMIN
     *
     * Позволяет быстро включить/выключить машину без обновления всех полей.
     * Используется для снятия машины с продаж (техобслуживание, ремонт).
     *
     * @param id ID автомобиля
     * @param available новое значение доступности (true/false)
     * @return ResponseEntity с кодом 200 OK при успехе или 404 если не найден
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Void> updateAvailability(@PathVariable Long id,
                                                   @RequestParam boolean available) {
        if (carService.findById(id).isPresent()) {
            carService.setAvailability(id, available);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ПРЕОБРАЗОВАНИЯ ==========

    /**
     * Конвертация Entity → DTO.
     *
     * Преобразует внутреннюю модель Car в публичный CarDTO.
     * Скрывает служебные поля (createdAt, updatedAt) от клиентов.
     *
     * @param car объект Entity из БД
     * @return объект DTO для отправки клиенту
     */
    private CarDTO convertToDTO(Car car) {
        CarDTO dto = new CarDTO();
        dto.setId(car.getId());
        dto.setBrand(car.getMake());
        dto.setModel(car.getModel());
        dto.setPricePerDay(car.getDailyPrice());
        dto.setVehicleClass(car.getVehicleClass());
        dto.setTransmission(car.getTransmission());
        dto.setYear(car.getYear());
        dto.setAvailable(car.isAvailable());
        dto.setPhotoUrl(car.getPhotoUrl());
        dto.setCatalog(car.getCatalog().name());
        return dto;
    }

    /**
     * Конвертация DTO → Entity.
     *
     * Преобразует данные от клиента (CarDTO) во внутреннюю модель (Car).
     * Используется при создании нового автомобиля.
     *
     * @param dto объект DTO от клиента
     * @return новый объект Entity для сохранения в БД
     */
    private Car convertToEntity(CarDTO dto) {
        Car car = new Car();
        car.setMake(dto.getBrand());
        car.setModel(dto.getModel());
        car.setDailyPrice(dto.getPricePerDay());
        car.setVehicleClass(dto.getVehicleClass());
        car.setTransmission(dto.getTransmission());
        car.setYear(dto.getYear());
        car.setAvailable(dto.isAvailable());
        car.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getCatalog() != null) {
            car.setCatalog(CatalogType.valueOf(dto.getCatalog().toUpperCase()));
        }
        return car;
    }

    /**
     * Обновление существующего Entity из DTO.
     *
     * Применяет изменения от клиента к существующему объекту Car.
     * Используется при обновлении автомобиля (PUT запрос).
     * Проверяет каждое поле на null перед обновлением.
     *
     * @param car существующий объект Entity
     * @param dto объект DTO с новыми данными
     */
    private void updateCarFromDTO(Car car, CarDTO dto) {
        if (dto.getBrand() != null) car.setMake(dto.getBrand());
        if (dto.getModel() != null) car.setModel(dto.getModel());
        if (dto.getPricePerDay() != null) car.setDailyPrice(dto.getPricePerDay());
        if (dto.getVehicleClass() != null) car.setVehicleClass(dto.getVehicleClass());
        if (dto.getTransmission() != null) car.setTransmission(dto.getTransmission());
        if (dto.getYear() > 0) car.setYear(dto.getYear());
        car.setAvailable(dto.isAvailable());
        if (dto.getPhotoUrl() != null) car.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getCatalog() != null) {
            car.setCatalog(CatalogType.valueOf(dto.getCatalog().toUpperCase()));
        }
    }
}
