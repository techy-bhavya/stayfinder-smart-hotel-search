package com.stayfinder.app.repository;

import com.stayfinder.app.model.Booking;
import com.stayfinder.app.model.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        select case when count(b) > 0 then true else false end
        from Booking b
        where b.room.id = :roomId
          and b.status = com.stayfinder.app.model.BookingStatus.CONFIRMED
          and b.checkIn < :checkOut
          and b.checkOut > :checkIn
        """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @EntityGraph(attributePaths = {"room", "room.hotel", "user"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"room", "room.hotel", "user"})
    List<Booking> findAllByOrderByCreatedAtDesc();

    long countByStatus(BookingStatus status);
}
