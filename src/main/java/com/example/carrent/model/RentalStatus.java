package com.example.carrent.model;

public enum RentalStatus {
    PENDING,    // заявка клиента, ждёт решения менеджера
    ACTIVE,     // одобрено, в процессе аренды
    REJECTED,   // отклонено менеджером
    CANCELLED,  // отменено клиентом
    COMPLETED   // завершено менеджером
}

