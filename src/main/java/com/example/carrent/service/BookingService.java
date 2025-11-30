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
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Сервис управления бронированиями автомобилей.
 *
 * Центральный компонент бизнес-логики для работы с арендой.
 * Обеспечивает:
 * - Создание и валидацию бронирований
 * - Проверку доступности машин на указанные даты
 * - Управление жизненным циклом аренды (одобрение, отклонение, завершение, отмена)
 * - Автоматическую синхронизацию доступности автомобилей
 * - Контроль прав доступа пользователей
 *
 * Этот сервис объединяет функционал управления бронированиями и арендами.
 *
 * @author Система аренды автомобилей
 */
@Service
@Transactional // Все публичные методы выполняются в транзакции БД
public class BookingService {

    /**
     * Множество статусов аренды, при которых автомобиль считается занятым.
     *
     * PENDING - заявка ждет одобрения менеджером, машина зарезервирована
     * ACTIVE - аренда активна, автомобиль используется клиентом
     *
     * REJECTED, COMPLETED, CANCELLED не блокируют машину.
     */
    private static final Set<RentalStatus> HOLDS_AVAILABILITY =
            EnumSet.of(RentalStatus.PENDING, RentalStatus.ACTIVE);

    // Зависимости - репозитории для доступа к данным
    private final RentalRepository rentalRepo;
    private final CarRepository carRepo;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;

    /**
     * Конструктор с автоматическим внедрением зависимостей Spring.
     *
     * @param rentalRepo репозиторий для работы с арендами
     * @param carRepo репозиторий для работы с автомобилями
     * @param userRepo репозиторий для работы с пользователями
     * @param customerRepo репозиторий для работы с профилями клиентов
     */
    public BookingService(RentalRepository rentalRepo,
                          CarRepository carRepo,
                          UserRepository userRepo,
                          CustomerRepository customerRepo) {
        this.rentalRepo = rentalRepo;
        this.carRepo = carRepo;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
    }

    /**
     * Создать новое бронирование автомобиля.
     *
     * Последовательность действий:
     * 1. Валидация входных дат (не в прошлом, конец после начала)
     * 2. Проверка занятости машины на указанный период
     * 3. Загрузка данных автомобиля и клиента из БД
     * 4. Создание записи Rental со статусом PENDING
     * 5. Автоматический расчет количества дней аренды
     * 6. Расчет общей стоимости на основе текущей цены машины
     * 7. Сохранение бронирования в БД
     * 8. Скрытие машины из списка доступных (available = false)
     *
     * Примечания:
     * - Количество дней рассчитывается включительно (startDate и endDate входят)
     * - Цена фиксируется на момент бронирования (может отличаться от текущей)
     * - Машина становится недоступной даже для статуса PENDING
     *
     * @param carId ID автомобиля для аренды
     * @param username логин пользователя (для поиска профиля клиента)
     * @param startDate дата начала аренды (не может быть в прошлом)
     * @param endDate дата окончания аренды (должна быть после startDate)
     * @return сохраненный объект Rental с присвоенным ID
     * @throws IllegalArgumentException если даты невалидны или машина занята
     * @throws RuntimeException если автомобиль/пользователь/клиент не найдены
     */
    public Rental book(Long carId, String username, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата начала аренды не может быть в прошлом");
        }
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        boolean hasOverlap = rentalRepo.hasOverlap(carId, startDate, endDate);
        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "Автомобиль уже забронирован на выбранные даты. " +
                            "Пожалуйста, выберите другой период или другой автомобиль.");
        }

        Car car = carRepo.findById(carId)
                .orElseThrow(() -> new RuntimeException("Автомобиль с ID " + carId + " не найден"));

        Customer customer = customerRepo.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException(
                        "Профиль клиента для пользователя " + username + " не найден"));

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStatus(RentalStatus.PENDING);

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        rental.setDaysCount((int) days);
        rental.setPricePerDay(car.getDailyPrice());
        rental.setTotalAmount(
                car.getDailyPrice().multiply(java.math.BigDecimal.valueOf(days))
        );

        Rental saved = rentalRepo.save(rental);
        car.setAvailable(false);
        carRepo.save(car);

        return saved;
    }

    /**
     * Одобрить бронирование менеджером (PENDING → ACTIVE).
     *
     * Переводит аренду из статуса ожидания в активный статус.
     * Только менеджер или администратор может выполнить эту операцию
     * (проверка прав осуществляется через @PreAuthorize в контроллере).
     *
     * После одобрения клиент может получить автомобиль.
     *
     * @param rentalId ID бронирования для одобрения
     * @return обновленный объект Rental со статусом ACTIVE
     * @throws RuntimeException если бронирование с указанным ID не найдено
     */
    public Rental approve(Long rentalId) {
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));

        rental.setStatus(RentalStatus.ACTIVE);
        return rentalRepo.save(rental);
    }

    /**
     * Отклонить бронирование менеджером (PENDING → REJECTED).
     *
     * Менеджер или администратор может отклонить заявку клиента.
     * После отклонения автомобиль может быть освобожден для других клиентов,
     * если на него больше нет активных/ожидающих броней.
     *
     * Процесс:
     * 1. Проверка прав пользователя (MANAGER или ADMIN)
     * 2. Изменение статуса на REJECTED
     * 3. Автоматическая проверка необходимости освобождения машины
     *
     * @param rentalId ID бронирования для отклонения
     * @param username логин пользователя, выполняющего операцию
     * @return обновленный объект Rental со статусом REJECTED
     * @throws RuntimeException если бронирование не найдено
     * @throws AccessDeniedException если пользователь не является менеджером/админом
     */
    public Rental reject(Long rentalId, String username) {
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));

        // Проверка прав доступа
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isManagerOrAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MANAGER") ||
                        r.getName().equals("ROLE_ADMIN"));

        if (!isManagerOrAdmin) {
            throw new AccessDeniedException(
                    "Только менеджер или администратор может отклонять бронирования");
        }

        rental.setStatus(RentalStatus.REJECTED);
        Rental saved = rentalRepo.save(rental);
        updateCarAvailabilityIfFree(rental.getCar());
        return saved;
    }

    /**
     * Отменить бронирование клиентом (любой статус → CANCELLED).
     *
     * Клиент может отменить ТОЛЬКО СВОЮ бронь.
     * После отмены автомобиль может быть освобожден для других клиентов,
     * если на него больше нет активных/ожидающих броней.
     *
     * Процесс:
     * 1. Проверка владельца брони (совпадает ли username)
     * 2. Изменение статуса на CANCELLED
     * 3. Автоматическая проверка необходимости освобождения машины
     *
     * @param rentalId ID бронирования для отмены
     * @param username логин пользователя (для проверки владельца)
     * @return обновленный объект Rental со статусом CANCELLED
     * @throws RuntimeException если бронирование не найдено
     * @throws AccessDeniedException если бронь принадлежит другому клиенту
     */
    public Rental cancel(Long rentalId, String username) {
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));

        String ownerUsername = rental.getCustomer().getUser().getUsername();
        if (!ownerUsername.equals(username)) {
            throw new AccessDeniedException(
                    "Вы можете отменить только свои собственные бронирования");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        Rental saved = rentalRepo.save(rental);
        updateCarAvailabilityIfFree(rental.getCar());
        return saved;
    }

    /**
     * Завершить аренду менеджером (ACTIVE → COMPLETED).
     *
     * Менеджер отмечает, что аренда завершена (автомобиль возвращен).
     * После завершения машина может быть освобождена для других клиентов.
     *
     * @param rentalId ID бронирования для завершения
     * @return обновленный объект Rental со статусом COMPLETED
     * @throws RuntimeException если бронирование не найдено
     */
    public Rental complete(Long rentalId) {
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));

        rental.setStatus(RentalStatus.COMPLETED);
        Rental saved = rentalRepo.save(rental);
        updateCarAvailabilityIfFree(rental.getCar());
        return saved;
    }

    /**
     * Завершить аренду клиентом (ACTIVE → COMPLETED).
     *
     * Клиент может отметить свою аренду как завершенную.
     * Отличие от complete(): проверяет, что клиент является владельцем брони.
     *
     * @param username логин клиента
     * @param rentalId ID бронирования
     * @return обновленный объект Rental со статусом COMPLETED
     * @throws RuntimeException если бронирование не найдено
     * @throws AccessDeniedException если бронь принадлежит другому клиенту
     */
    public Rental completeByCustomer(String username, Long rentalId) {
        Rental rental = rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));

        String ownerUsername = rental.getCustomer().getUser().getUsername();
        if (!ownerUsername.equals(username)) {
            throw new AccessDeniedException(
                    "Вы можете завершить только свои собственные бронирования");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        Rental saved = rentalRepo.save(rental);
        updateCarAvailabilityIfFree(rental.getCar());
        return saved;
    }

    /**
     * Проверить и обновить доступность автомобиля.
     *
     * Машина помечается как available=true ТОЛЬКО если на нее нет
     * активных или ожидающих броней (статусы PENDING или ACTIVE).
     *
     * Этот метод предотвращает ситуацию, когда машина освобождается
     * при наличии других активных бронирований.
     *
     * Логика:
     * - Если есть хотя бы одна бронь с PENDING или ACTIVE → машина остается unavailable
     * - Если все брони REJECTED, COMPLETED или CANCELLED → машина становится available
     *
     * @param car автомобиль для проверки и обновления
     */
    private void updateCarAvailabilityIfFree(Car car) {
        boolean hasActiveRentals = rentalRepo.existsByCar_IdAndStatusIn(
                car.getId(), HOLDS_AVAILABILITY);

        if (!hasActiveRentals) {
            car.setAvailable(true);
            carRepo.save(car);
        }
    }

    /**
     * Получить все бронирования конкретного клиента.
     *
     * Результаты отсортированы по дате создания (новые первыми).
     * Используется в личном кабинете клиента для отображения истории броней.
     *
     * @param customerId ID профиля клиента
     * @return список всех бронирований клиента (включая все статусы)
     */
    public List<Rental> findCustomerRentals(Long customerId) {
        return rentalRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Получить бронирование по ID.
     *
     * @param rentalId ID бронирования
     * @return объект Rental
     * @throws RuntimeException если бронирование не найдено
     */
    public Rental findById(Long rentalId) {
        return rentalRepo.findById(rentalId)
                .orElseThrow(() -> new RuntimeException(
                        "Бронирование с ID " + rentalId + " не найдено"));
    }

    /**
     * Получить все бронирования в системе.
     *
     * Используется менеджером для просмотра всех броней.
     * Может возвращать большой объем данных - использовать с осторожностью.
     *
     * @return список всех бронирований
     */
    public List<Rental> findAll() {
        return rentalRepo.findAll();
    }

    /**
     * Получить все бронирования с определенным статусом.
     *
     * Результаты отсортированы по дате создания (новые первыми).
     * Используется менеджером для фильтрации броней (например, только PENDING).
     *
     * @param status статус для фильтрации (PENDING, ACTIVE, REJECTED, COMPLETED, CANCELLED)
     * @return список бронирований с указанным статусом
     */
    public List<Rental> findByStatus(RentalStatus status) {
        return rentalRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Проверить доступность автомобиля на указанный период дат.
     *
     * Проверяет, свободен ли автомобиль на указанные даты.
     * Учитываются только активные и ожидающие бронирования (PENDING, ACTIVE).
     * Отмененные и завершенные бронирования не блокируют автомобиль.
     *
     * @param carId ID автомобиля
     * @param startDate дата начала проверяемого периода
     * @param endDate дата окончания проверяемого периода
     * @return true если автомобиль доступен на указанные даты, false если занят
     */
    public boolean isCarAvailableForDates(Long carId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return false;
        }
        return !rentalRepo.hasOverlap(carId, startDate, endDate);
    }
}
