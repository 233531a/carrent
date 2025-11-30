package com.example.carrent.service;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import com.example.carrent.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления автомобилями.
 *
 * Предоставляет бизнес-логику для работы с автомобилями:
 * - Поиск и фильтрация автомобилей
 * - Управление доступностью
 * - Работа с каталогами
 *
 * Все методы выполняются в транзакции (@Transactional).
 *
 * @author Система аренды автомобилей
 */
@Service
@Transactional
public class CarService {

    /**
     * Репозиторий для работы с автомобилями в БД.
     */
    private final CarRepository carRepository;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param carRepository репозиторий автомобилей
     */
    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Получить все автомобили.
     *
     * @return список всех автомобилей в системе
     */
    public List<Car> findAll() {
        return carRepository.findAll();
    }

    /**
     * Получить автомобиль по идентификатору.
     *
     * @param id ID автомобиля
     * @return Optional с автомобилем, если найден, иначе пустой Optional
     */
    public Optional<Car> findById(Long id) {
        return carRepository.findById(id);
    }

    /**
     * Сохранить или обновить автомобиль.
     *
     * Если у автомобиля есть ID - обновляет существующий.
     * Если ID отсутствует - создает новый.
     *
     * @param car автомобиль для сохранения
     * @return сохраненный автомобиль с присвоенным ID
     */
    public Car save(Car car) {
        return carRepository.save(car);
    }

    /**
     * Удалить автомобиль по идентификатору.
     *
     * ВНИМАНИЕ: Перед удалением рекомендуется проверить,
     * нет ли активных бронирований на этот автомобиль.
     *
     * @param id ID автомобиля для удаления
     */
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }

    /**
     * Получить все автомобили указанного типа каталога.
     *
     * @param catalog тип каталога (REGULAR, TAXI, DELIVERY)
     * @return список автомобилей указанного типа
     */
    public List<Car> findByCatalog(CatalogType catalog) {
        return carRepository.findByCatalog(catalog);
    }

    /**
     * Получить автомобили указанного типа каталога с пагинацией.
     *
     * @param catalog тип каталога (REGULAR, TAXI, DELIVERY)
     * @param pageable параметры пагинации
     * @return страница с автомобилями указанного типа
     */
    public Page<Car> findByCatalog(CatalogType catalog, Pageable pageable) {
        return carRepository.findByCatalog(catalog, pageable);
    }

    /**
     * Поиск автомобилей с фильтрами и пагинацией.
     *
     * Поддерживает фильтрацию по:
     * - текстовому запросу (поиск в марке и модели)
     * - классу автомобиля
     * - типу коробки передач
     * - максимальной цене за день
     *
     * @param query текстовый запрос для поиска
     * @param vehicleClass класс автомобиля (опционально)
     * @param transmission тип коробки передач (опционально)
     * @param maxPrice максимальная цена за день (опционально)
     * @param pageable параметры пагинации
     * @return страница с отфильтрованными автомобилями
     */
    public Page<Car> search(String query, String vehicleClass, String transmission,
                            BigDecimal maxPrice, Pageable pageable) {
        return carRepository.search(query, vehicleClass, transmission, maxPrice, pageable);
    }

    /**
     * Проверить доступность автомобиля.
     *
     * @param carId ID автомобиля
     * @return true если автомобиль доступен, false если недоступен или не найден
     */
    public boolean isAvailable(Long carId) {
        return carRepository.findById(carId)
                .map(Car::isAvailable)
                .orElse(false);
    }

    /**
     * Установить доступность автомобиля.
     *
     * Используется для снятия автомобиля с продаж (техобслуживание, ремонт)
     * или возврата в каталог после освобождения.
     *
     * @param carId ID автомобиля
     * @param available true для доступности, false для недоступности
     */
    public void setAvailability(Long carId, boolean available) {
        carRepository.findById(carId).ifPresent(car -> {
            car.setAvailable(available);
            carRepository.save(car);
        });
    }
}
