package com.example.carrent.service;

import com.example.carrent.model.Car;
import com.example.carrent.model.Customer;
import com.example.carrent.model.Rental;
import com.example.carrent.model.RentalStatus;
import com.example.carrent.model.User;
import com.example.carrent.repository.CarRepository;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
public class BookingService {

    private static final Set<RentalStatus> HOLDS_AVAILABILITY =
            EnumSet.of(RentalStatus.PENDING, RentalStatus.ACTIVE);

    private final RentalRepository rentalRepo;
    private final CarRepository carRepo;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;

    public BookingService(RentalRepository rentalRepo,
                          CarRepository carRepo,
                          UserRepository userRepo,
                          CustomerRepository customerRepo) {
        this.rentalRepo = rentalRepo;
        this.carRepo = carRepo;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
    }

    /** Пользователь подаёт заявку (PENDING) — авто сразу скрываем (available=false). */
    @Transactional
    public Rental book(String username, Long carId, LocalDate startDate, LocalDate endDate) {
        User user = userRepo.findByUsername(username).orElseThrow();
        Customer customer = customerRepo.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setUser(user);
                    return customerRepo.save(c);
                });
        Car car = carRepo.findById(carId).orElseThrow();

        // Не даём бронировать уже скрытую/занятую машину
        if (!car.isAvailable()) {
            throw new IllegalStateException("Автомобиль уже недоступен для бронирования");
        }

        Rental r = new Rental();
        r.setCar(car);
        r.setCustomer(customer);
        r.setStartDate(startDate);
        r.setEndDate(endDate);
        r.setStatus(RentalStatus.PENDING);

        // Сохраняем заявку
        r = rentalRepo.save(r);

        // Сразу скрываем карточку
        car.setAvailable(false);
        carRepo.save(car);

        return r;
    }

    /** Отмена доступна только для заявок (PENDING) и только владельцу. */
    @Transactional
    public void cancel(String username, Long rentalId) {
        Rental r = rentalRepo.findById(rentalId).orElseThrow();
        String owner = r.getCustomer().getUser().getUsername();
        if (!owner.equals(username)) {
            throw new AccessDeniedException("Нельзя отменить чужую заявку");
        }
        if (r.getStatus() != RentalStatus.PENDING) {
            throw new IllegalStateException("Отменять можно только заявки в статусе «Ожидание»");
        }
        r.setStatus(RentalStatus.CANCELLED);

        // Проверяем, можно ли снова показывать карточку
        updateCarAvailabilityIfFree(r.getCar());
    }

    /** Менеджер одобряет только PENDING -> ACTIVE (машина уже скрыта; ничего не меняем). */
    @Transactional
    public void approveByManager(Long rentalId) {
        var r = rentalRepo.findById(rentalId).orElseThrow();
        if (r.getStatus() != RentalStatus.PENDING)
            throw new IllegalStateException("Одобрять можно только PENDING");
        r.setStatus(RentalStatus.ACTIVE);
        // car уже hidden; без действий
    }

    /** Менеджер отклоняет только PENDING -> REJECTED. */
    @Transactional
    public void rejectByManager(Long rentalId) {
        Rental r = rentalRepo.findById(rentalId).orElseThrow();
        if (r.getStatus() != RentalStatus.PENDING) {
            throw new IllegalStateException("Отклонять можно только заявки в статусе «Ожидание»");
        }
        r.setStatus(RentalStatus.REJECTED);

        updateCarAvailabilityIfFree(r.getCar());
    }

    /** Менеджер завершает только ACTIVE -> COMPLETED. */
    @Transactional
    public void completeByManager(Long rentalId) {
        Rental r = rentalRepo.findById(rentalId).orElseThrow();
        if (r.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Завершать можно только активные аренды");
        }
        r.setStatus(RentalStatus.COMPLETED);

        updateCarAvailabilityIfFree(r.getCar());
    }

    /** Клиент завершает только свою ACTIVE -> COMPLETED. */
    @Transactional
    public void completeByCustomer(String username, Long rentalId) {
        Rental r = rentalRepo.findById(rentalId).orElseThrow();
        String owner = r.getCustomer().getUser().getUsername();
        if (!owner.equals(username)) {
            throw new AccessDeniedException("Нельзя завершить чужую аренду");
        }
        if (r.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Завершать можно только активные аренды");
        }
        r.setStatus(RentalStatus.COMPLETED);

        updateCarAvailabilityIfFree(r.getCar());
    }

    /** Если по машине больше нет заявок/активных — делаем её видимой. */
    private void updateCarAvailabilityIfFree(Car car) {
        boolean stillHeld = rentalRepo.existsByCar_IdAndStatusIn(car.getId(), HOLDS_AVAILABILITY);
        if (!stillHeld) {
            car.setAvailable(true);
            carRepo.save(car);
        }
    }
}
