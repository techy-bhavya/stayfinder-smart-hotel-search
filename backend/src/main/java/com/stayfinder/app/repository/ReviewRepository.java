package com.stayfinder.app.repository;

import com.stayfinder.app.model.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"user", "hotel"})
    List<Review> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    Optional<Review> findByUserIdAndHotelId(Long userId, Long hotelId);
}
