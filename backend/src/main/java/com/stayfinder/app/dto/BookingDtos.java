package com.stayfinder.app.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class BookingDtos {
    private BookingDtos() {}

    public record BookingRequest(
            @NotNull Long roomId,
            @NotNull @FutureOrPresent LocalDate checkIn,
            @NotNull LocalDate checkOut,
            @Min(1) int guests
    ) {}

    public record BookingResponse(
            Long id,
            Long hotelId,
            String hotelName,
            String city,
            String imageUrl,
            Long roomId,
            String roomType,
            LocalDate checkIn,
            LocalDate checkOut,
            int guests,
            long nights,
            BigDecimal totalAmount,
            String status,
            Instant createdAt
    ) {}
}
