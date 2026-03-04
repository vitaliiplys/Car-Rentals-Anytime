package com.example.carsharingservice.repository.rental;

import com.example.carsharingservice.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @EntityGraph(attributePaths = {"user", "car"})
    Page<Rental> findAllByUserIdAndActive(Long userId, boolean active, Pageable pageable);

    Optional<Rental> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"user", "car"})
    List<Rental> findAllByActiveTrueAndReturnDateBefore(LocalDate date);
}
