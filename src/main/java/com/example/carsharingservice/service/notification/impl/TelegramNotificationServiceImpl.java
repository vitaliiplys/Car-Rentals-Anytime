package com.example.carsharingservice.service.notification.impl;

import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.TelegramChat;
import com.example.carsharingservice.repository.telegram.TelegramChatRepository;
import com.example.carsharingservice.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements NotificationService {
    private static final String TELEGRAM_API_URL =
            "https://api.telegram.org/bot%s/sendMessage";

    private final RestTemplate restTemplate;
    private final TelegramChatRepository telegramChatRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        String message = String.format(
                "Payment Successful!%n%n"
                        + "Payment ID: %d%n"
                        + "Type: %s%n"
                        + "Amount: $%.2f%n"
                        + "Status: %s%n"
                        + "Session ID: %s",
                payment.getId(),
                payment.getType(),
                payment.getAmountToPay(),
                payment.getStatus(),
                payment.getSessionId()
        );
        sendToAllChats(message);
    }

    @Override
    public void sendRentalCreatedNotification(Rental rental) {
        String message = String.format(
                "New Rental Created!%n%n"
                        + "Rental ID: %d%n"
                        + "User ID: %d%n"
                        + "Car ID: %d%n"
                        + "Rental Date: %s%n"
                        + "Return Date: %s",
                rental.getId(),
                rental.getUser().getId(),
                rental.getCar().getId(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );
        sendToAllChats(message);
    }

    @Override
    public void sendOverdueRentalsNotification(List<Rental> overdueRentals) {
        String message;
        if (overdueRentals.isEmpty()) {
            message = "No rentals overdue today!";
        } else {
            StringBuilder sb = new StringBuilder("Overdue Rentals!\n");
            for (Rental rental : overdueRentals) {
                long daysOverdue = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());
                sb.append(String.format(
                        "%nRental ID: %d%n"
                                + "User ID: %d%n"
                                + "Car ID: %d%n"
                                + "Return Date: %s%n"
                                + "Days Overdue: %d",
                        rental.getId(),
                        rental.getUser().getId(),
                        rental.getCar().getId(),
                        rental.getReturnDate(),
                        daysOverdue
                ));
            }
            message = sb.toString();
        }
        sendToAllChats(message);
    }

    @Override
    public void saveChatId(Long chatId) {
        if (telegramChatRepository.existsByChatId(chatId)) {
            log.info("Telegram chat ID {} is already registered.", chatId);
            return;
        }
        TelegramChat chat = new TelegramChat();
        chat.setChatId(chatId);
        telegramChatRepository.save(chat);
        log.info("Telegram chat ID {} saved to database.", chatId);
    }

    private void sendToAllChats(String message) {
        List<TelegramChat> chats = telegramChatRepository.findAll();
        if (chats.isEmpty()) {
            log.warn("No Telegram chat IDs registered. Skipping notification.");
            return;
        }
        String url = String.format(TELEGRAM_API_URL, botToken);
        for (TelegramChat chat : chats) {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("chat_id", String.valueOf(chat.getChatId()));
                request.put("text", message);
                restTemplate.postForObject(url, request, String.class);
            } catch (Exception e) {
                log.error("Failed to send Telegram message to chat {}: {}",
                        chat.getChatId(), e.getMessage());
            }
        }
    }
}
