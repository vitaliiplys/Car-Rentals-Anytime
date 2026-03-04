package com.example.carsharingservice.service.notification.impl;

import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.repository.rental.RentalRepository;
import com.example.carsharingservice.service.notification.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueRentalScheduler {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals =
                rentalRepository.findAllByActiveTrueAndReturnDateBefore(LocalDate.now());
        log.info("Overdue rentals check: found {} overdue rental(s).", overdueRentals.size());
        notificationService.sendOverdueRentalsNotification(overdueRentals);
    }
}
