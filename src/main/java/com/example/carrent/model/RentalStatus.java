package com.example.carrent.model;

public enum RentalStatus {
    PENDING("Ожидание"),
    ACTIVE("Активна"),
    REJECTED("Отклонена"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String titleRu;

    RentalStatus(String titleRu) {
        this.titleRu = titleRu;
    }

    public String getTitleRu() {
        return titleRu;
    }
}
