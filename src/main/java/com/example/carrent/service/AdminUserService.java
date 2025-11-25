package com.example.carrent.service;

import com.example.carrent.model.User;
import com.example.carrent.repository.CustomerRepository;
import com.example.carrent.repository.RentalRepository;
import com.example.carrent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для административных операций с пользователями.
 *
 * Предоставляет методы для критических операций, доступных только администраторам:
 * - Полное удаление пользователя со всеми связанными данными
 *
 * Все операции выполняются в транзакции для обеспечения целостности данных.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
@Service
public class AdminUserService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepo;

    /**
     * Репозиторий для работы с профилями клиентов.
     */
    private final CustomerRepository customerRepo;

    /**
     * Репозиторий для работы с арендами.
     */
    private final RentalRepository rentalRepo;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userRepo репозиторий пользователей
     * @param customerRepo репозиторий клиентов
     * @param rentalRepo репозиторий аренд
     */
    public AdminUserService(UserRepository userRepo,
                            CustomerRepository customerRepo,
                            RentalRepository rentalRepo) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.rentalRepo = rentalRepo;
    }

    /**
     * Полностью удалить пользователя и все связанные данные.
     *
     * ВНИМАНИЕ: Это критическая операция, которая удаляет:
     * 1. Все аренды пользователя (если он был клиентом)
     * 2. Профиль клиента (если существует)
     * 3. Саму учетную запись пользователя
     *
     * Операция выполняется в транзакции для обеспечения целостности данных.
     * При ошибке на любом этапе все изменения откатываются.
     *
     * @param userId ID пользователя для удаления
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public void deleteUserHard(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();

        var customer = customerRepo.findByUser_Id(userId).orElse(null);
        if (customer != null) {
            rentalRepo.deleteByCustomer_Id(customer.getId());
            customerRepo.delete(customer);
        }
        userRepo.delete(user);
    }
}
