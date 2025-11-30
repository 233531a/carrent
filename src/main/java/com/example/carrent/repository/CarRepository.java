package com.example.carrent.repository;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Репозиторий для работы с автомобилями.
 *
 * Предоставляет методы для поиска, фильтрации и пагинации автомобилей.
 * Наследует стандартные методы CRUD от JpaRepository.
 *
 * @author Система аренды автомобилей
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Найти все автомобили указанного типа каталога.
     *
     * @param catalog тип каталога (REGULAR, TAXI, DELIVERY)
     * @return список автомобилей указанного типа
     */
    List<Car> findByCatalog(CatalogType catalog);

    /**
     * Найти все автомобили указанного типа каталога с пагинацией.
     *
     * @param catalog тип каталога (REGULAR, TAXI, DELIVERY)
     * @param pageable параметры пагинации (номер страницы, размер, сортировка)
     * @return страница с автомобилями указанного типа
     */
    Page<Car> findByCatalog(CatalogType catalog, Pageable pageable);

    /**
     * Поиск автомобилей с фильтрами и пагинацией.
     *
     * Поддерживает поиск по:
     * - текстовому запросу (поиск в марке и модели)
     * - классу автомобиля
     * - типу коробки передач
     * - максимальной цене за день
     *
     * Все параметры опциональны (null означает отсутствие фильтра).
     * Поиск выполняется без учета регистра.
     *
     * @param q текстовый запрос для поиска в марке и модели
     * @param cls класс автомобиля (например, "economy", "business")
     * @param gear тип коробки передач (например, "AT", "MT")
     * @param maxPrice максимальная цена за день аренды
     * @param pageable параметры пагинации
     * @return страница с отфильтрованными автомобилями
     */
    @Query("""
       select c from Car c
       where (:q is null or lower(c.make) like lower(concat('%',:q,'%'))
                       or lower(c.model) like lower(concat('%',:q,'%')))
         and (:cls is null or lower(c.vehicleClass) = lower(:cls))
         and (:gear is null or lower(c.transmission) = lower(:gear))
         and (:maxPrice is null or c.dailyPrice <= :maxPrice)
       """)
    Page<Car> search(String q, String cls, String gear, BigDecimal maxPrice, Pageable pageable);
}
