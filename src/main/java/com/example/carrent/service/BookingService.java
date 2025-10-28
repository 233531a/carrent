package com.example.carrent.service;

import com.example.carrent.model.Car;
import com.example.carrent.model.Customer;
import com.example.carrent.model.Rental;
import com.example.carrent.model.RentalStatus;
import com.example.carrent.repository.CarRepository;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    private final RentalRepository rentalRepo;
    private final CarRepository carRepo;
    private final CustomerRepository customerRepo;

    public BookingService(RentalRepository rentalRepo, CarRepository carRepo, CustomerRepository customerRepo) {
        this.rentalRepo = rentalRepo;
        this.carRepo = carRepo;
        this.customerRepo = customerRepo;
    }

    /**
     * Бронирование по username (principal.getUsername()).
     */
    @Transactional
    public Rental book(String username, Long carId, LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Некорректные даты брони");
        }

        Customer customer = customerRepo.findByUser_Username(username)
                .orElseThrow(() -> new IllegalStateException("Клиентская карточка не найдена"));

        Car car = carRepo.findById(carId).orElseThrow();

        // Пересечения по датам
        if (rentalRepo.hasOverlap(carId, start, end)) {
            throw new IllegalStateException("На выбранные даты авто уже забронировано");
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) throw new IllegalArgumentException("Минимум 1 день");

        BigDecimal pricePerDay = car.getDailyPrice();
        BigDecimal total = pricePerDay.multiply(BigDecimal.valueOf(days));

        Rental r = new Rental();
        r.setCar(car);
        r.setCustomer(customer);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setStatus(RentalStatus.PENDING);   // ждёт решения менеджера
        r.setDaysCount((int) days);
        r.setPricePerDay(pricePerDay);
        r.setTotalAmount(total);

        r = rentalRepo.save(r);

        // делаем авто недоступным при появлении ожидающей/активной брони
        blockCarIfNeeded(car);

        return r;
    }

    /**
     * Отмена брони пользователем (защита от отмены чужой брони).
     */
    @Transactional
    public void cancel(String username, Long rentalId) {
        Rental r = rentalRepo.findById(rentalId).orElseThrow();

        String ownerUsername = r.getCustomer().getUser().getUsername();
        if (ownerUsername == null || !ownerUsername.equals(username)) {
            throw new SecurityException("Нельзя отменить чужую бронь");
        }

        if (r.getStatus() == RentalStatus.CANCELLED || r.getStatus() == RentalStatus.COMPLETED) {
            return;
        }

        r.setStatus(RentalStatus.CANCELLED);
        rentalRepo.save(r);

        freeCarIfNoActiveOrPending(r.getCar());
    }

    // ---------------------------
    // Методы для менеджера/админа
    // ---------------------------

    /**
     * Одобрить бронь -> переводим в ACTIVE.
     */
    @Transactional
    public void approveByManager(Long rentalId) {
        Rental r = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Бронь не найдена: " + rentalId));

        if (r.getStatus() == RentalStatus.CANCELLED || r.getStatus() == RentalStatus.COMPLETED) {
            return; // ничего не делаем для финальных статусов
        }

        // На всякий случай: при одобрении удостоверимся, что авто заблокировано
        r.setStatus(RentalStatus.ACTIVE);
        rentalRepo.save(r);
        blockCarIfNeeded(r.getCar());
    }

    /**
     * Отклонить бронь -> REJECTED.
     */
    @Transactional
    public void rejectByManager(Long rentalId) {
        Rental r = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Бронь не найдена: " + rentalId));

        if (r.getStatus() == RentalStatus.CANCELLED || r.getStatus() == RentalStatus.COMPLETED) {
            return;
        }

        r.setStatus(RentalStatus.REJECTED);
        rentalRepo.save(r);

        freeCarIfNoActiveOrPending(r.getCar());
    }

    /**
     * Завершить аренду -> COMPLETED.
     */
    @Transactional
    public void completeByManager(Long rentalId) {
        Rental r = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Бронь не найдена: " + rentalId));

        if (r.getStatus() == RentalStatus.CANCELLED || r.getStatus() == RentalStatus.COMPLETED) {
            return;
        }

        r.setStatus(RentalStatus.COMPLETED);
        rentalRepo.save(r);

        freeCarIfNoActiveOrPending(r.getCar());
    }

    // ---------------------------
    // Вспомогательные методы
    // ---------------------------

    private void blockCarIfNeeded(Car car) {
        if (car == null) return;
        if (!car.isAvailable()) return;

        // если у авто есть PENDING/ACTIVE — держим его недоступным
        boolean blocked = rentalRepo.countByCarIdAndStatusIn(
                car.getId(), List.of(RentalStatus.PENDING, RentalStatus.ACTIVE)
        ) > 0;

        if (blocked) {
            car.setAvailable(false);
            carRepo.save(car);
        }
    }

    private void freeCarIfNoActiveOrPending(Car car) {
        if (car == null) return;

        boolean stillBlocked = rentalRepo.countByCarIdAndStatusIn(
                car.getId(), List.of(RentalStatus.PENDING, RentalStatus.ACTIVE)
        ) > 0;

        if (!stillBlocked && !car.isAvailable()) {
            car.setAvailable(true);
            carRepo.save(car);
        }
    }
}
