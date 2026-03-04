package com.example.carsharingservice.repository.payment;

import com.example.carsharingservice.model.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByRentalId_User_Id(@Param("userId") Long userId);

    Payment findBySessionId(String sessionId);
}
