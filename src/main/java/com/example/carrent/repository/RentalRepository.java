package com.example.carrent.repository;

import com.example.carrent.model.Rental;
import com.example.carrent.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    boolean existsByCar_IdAndStatusIn(Long carId, Collection<RentalStatus> statuses);

    // История броней клиента
    List<Rental> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Есть ли непогашенные брони, пересекающиеся по датам
    @Query("""
      select case when count(r) > 0 then true else false end
      from Rental r
      where r.car.id = :carId
        and r.status <> com.example.carrent.model.RentalStatus.CANCELLED
        and r.status <> com.example.carrent.model.RentalStatus.COMPLETED
        and (r.startDate <= :endDate and r.endDate >= :startDate)
    """)
    boolean hasOverlap(@Param("carId") Long carId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    // То же самое, но возвращает количество (если вдруг понадобится)
    @Query("""
      select count(r)
      from Rental r
      where r.car.id = :carId
        and r.status <> com.example.carrent.model.RentalStatus.CANCELLED
        and r.status <> com.example.carrent.model.RentalStatus.COMPLETED
        and (r.startDate <= :endDate and r.endDate >= :startDate)
    """)
    long countOverlaps(@Param("carId") Long carId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    // Сколько активных/ожидающих броней по авто — нужно, чтобы вернуть available=true после отмены
    long countByCarIdAndStatusIn(Long carId, List<RentalStatus> statuses);

    List<Rental> findAllByOrderByCreatedAtDesc();
    List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);

    // RentalRepository.java (добавь метод)
    void deleteByCustomer_Id(Long customerId);

}
