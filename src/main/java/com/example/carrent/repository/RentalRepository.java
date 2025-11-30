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

/**
 * Репозиторий для работы с арендами/бронированиями автомобилей.
 *
 * Предоставляет методы для поиска, фильтрации и проверки пересечений аренд.
 * Наследует стандартные методы CRUD от JpaRepository.
 *
 * @author Система аренды автомобилей
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    /**
     * Проверить существование аренд для автомобиля с указанными статусами.
     *
     * Используется для проверки, занят ли автомобиль (есть ли активные/ожидающие брони).
     *
     * @param carId ID автомобиля
     * @param statuses коллекция статусов для проверки
     * @return true если найдена хотя бы одна аренда с указанными статусами, false иначе
     */
    boolean existsByCar_IdAndStatusIn(Long carId, Collection<RentalStatus> statuses);

    /**
     * Получить историю всех бронирований клиента.
     *
     * Результаты отсортированы по дате создания (новые первыми).
     *
     * @param customerId ID профиля клиента
     * @return список всех бронирований клиента, отсортированный по дате создания (DESC)
     */
    List<Rental> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Проверить наличие пересекающихся активных бронирований.
     *
     * Проверяет, есть ли у автомобиля активные или ожидающие бронирования,
     * которые пересекаются с указанным периодом дат.
     * Отмененные и завершенные бронирования не учитываются.
     *
     * Используется при создании нового бронирования для проверки доступности автомобиля.
     *
     * @param carId ID автомобиля
     * @param startDate дата начала проверяемого периода
     * @param endDate дата окончания проверяемого периода
     * @return true если найдено пересечение, false если период свободен
     */
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

    /**
     * Подсчитать количество пересекающихся активных бронирований.
     *
     * Аналогично hasOverlap(), но возвращает количество вместо boolean.
     * Может быть полезно для отладки или статистики.
     *
     * @param carId ID автомобиля
     * @param startDate дата начала проверяемого периода
     * @param endDate дата окончания проверяемого периода
     * @return количество пересекающихся активных бронирований
     */
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

    /**
     * Подсчитать количество аренд автомобиля с указанными статусами.
     *
     * Используется для проверки, можно ли освободить автомобиль
     * (если нет активных/ожидающих броней).
     *
     * @param carId ID автомобиля
     * @param statuses список статусов для подсчета
     * @return количество аренд с указанными статусами
     */
    long countByCarIdAndStatusIn(Long carId, List<RentalStatus> statuses);

    /**
     * Получить все аренды, отсортированные по дате создания (новые первыми).
     *
     * @return список всех аренд, отсортированный по дате создания (DESC)
     */
    List<Rental> findAllByOrderByCreatedAtDesc();

    /**
     * Получить все аренды с указанным статусом, отсортированные по дате создания.
     *
     * @param status статус для фильтрации
     * @return список аренд с указанным статусом, отсортированный по дате создания (DESC)
     */
    List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);

    /**
     * Удалить все аренды клиента.
     *
     * Используется при удалении пользователя для каскадного удаления связанных данных.
     *
     * @param customerId ID профиля клиента
     */
    void deleteByCustomer_Id(Long customerId);
}
