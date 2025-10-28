package com.example.carrent.service;

import com.example.carrent.model.Car;
import com.example.carrent.model.Customer;
import com.example.carrent.model.Rental;
import com.example.carrent.model.RentalStatus;
import com.example.carrent.repository.CarRepository;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {

    private final RentalRepository rentalRepo;
    private final CarRepository carRepo;
    private final CustomerRepository customerRepo;

    public RentalService(RentalRepository rentalRepo, CarRepository carRepo, CustomerRepository customerRepo) {
        this.rentalRepo = rentalRepo;
        this.carRepo = carRepo;
        this.customerRepo = customerRepo;
    }

    /**
     * Создать бронирование. Делает авто недоступным, если бронирование успешно.
     */
    @Transactional
    public Long createBooking(Long carId, Long customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Некорректный диапазон дат.");
        }

        Car car = carRepo.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

        // Проверяем пересечения по датам
        boolean overlap = rentalRepo.hasOverlap(carId, startDate, endDate); // используем hasOverlap(...)
        if (overlap) {
            throw new IllegalStateException("На выбранные даты автомобиль уже забронирован.");
        }

        // Если у тебя в базе есть флаг доступности — можно дополнительно проверить
        if (!car.isAvailable()) {
            throw new IllegalStateException("Автомобиль недоступен для аренды.");
        }

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // включительно
        if (days <= 0) throw new IllegalArgumentException("Минимум 1 день.");

        BigDecimal total = car.getDailyPrice().multiply(BigDecimal.valueOf(days));

        Rental r = new Rental();
        r.setCar(car);
        r.setCustomer(customer);
        r.setStartDate(startDate);
        r.setEndDate(endDate);
        r.setStatus(RentalStatus.PENDING);   // <-- корректный enum
        r.setTotalAmount(total);             // <-- без pricePerDay/daysCount

        r = rentalRepo.save(r);

        // Помечаем авто недоступным (есть ожидающая/активная бронь)
        if (car.isAvailable()) {
            car.setAvailable(false);
            carRepo.save(car);
        }

        return r.getId();
    }

    /**
     * Отмена бронирования пользователем (по его customerId).
     * Возвращает авто в доступные, если по нему больше нет PENDING/ACTIVE броней.
     */
    @Transactional
    public void cancelBooking(Long rentalId, Long customerId) {
        Rental r = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Бронь не найдена"));

        if (!r.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("Нельзя отменить чужую бронь.");
        }

        if (r.getStatus() == RentalStatus.CANCELLED || r.getStatus() == RentalStatus.COMPLETED) {
            return; // уже окончена
        }

        r.setStatus(RentalStatus.CANCELLED);
        rentalRepo.save(r);

        // если по авто больше нет PENDING/ACTIVE — делаем его доступным снова
        boolean stillBlocked = rentalRepo.countByCarIdAndStatusIn(
                r.getCar().getId(),
                List.of(RentalStatus.PENDING, RentalStatus.ACTIVE)
        ) > 0;

        if (!stillBlocked && !r.getCar().isAvailable()) {
            Car car = r.getCar();
            car.setAvailable(true);
            carRepo.save(car);
        }
    }
}
