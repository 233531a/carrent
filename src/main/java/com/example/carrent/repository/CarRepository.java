package com.example.carrent.repository;

import com.example.carrent.model.Car;
import com.example.carrent.model.CatalogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByCatalog(CatalogType catalog);

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
