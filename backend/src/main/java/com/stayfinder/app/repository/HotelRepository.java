package com.stayfinder.app.repository;

import com.stayfinder.app.model.Hotel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @EntityGraph(attributePaths = {"rooms", "amenities"})
    List<Hotel> findByActiveTrue();

    @EntityGraph(attributePaths = {"rooms", "amenities"})
    Optional<Hotel> findByIdAndActiveTrue(Long id);
}
