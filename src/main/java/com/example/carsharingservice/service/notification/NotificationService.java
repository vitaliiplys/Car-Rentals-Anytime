package com.example.carsharingservice.service.notification;

import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import java.util.List;

public interface NotificationService {
    void sendPaymentSuccessNotification(Payment payment);

    void sendRentalCreatedNotification(Rental rental);

    void sendOverdueRentalsNotification(List<Rental> overdueRentals);

    void saveChatId(Long chatId);
}
