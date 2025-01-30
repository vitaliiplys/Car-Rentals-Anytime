package com.example.carsharingservice.repository.rental;

import com.example.carsharingservice.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @EntityGraph(attributePaths = {"user", "car"})
    Page<Rental> findAllByUserIdAndActive(Long userId, boolean active, Pageable pageable);
}
