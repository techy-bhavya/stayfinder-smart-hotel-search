package com.stayfinder.app.repository;

import com.stayfinder.app.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelIdAndActiveTrueOrderByPricePerNightAsc(Long hotelId);
    long countByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r join fetch r.hotel where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
