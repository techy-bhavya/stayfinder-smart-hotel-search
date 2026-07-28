package com.stayfinder.app.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HotelDtos {
    private HotelDtos() {}

    public record SearchCriteria(
            String query,
            String city,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Set<String> amenities,
            LocalDate checkIn,
            LocalDate checkOut,
            int page,
            int size
    ) {}

    public record ScoreBreakdown(
            double total,
            double textMatch,
            double rating,
            double priceValue,
            double amenityMatch,
            double popularity
    ) {}

    public record HotelCardResponse(
            Long id,
            String name,
            String city,
            String area,
            String imageUrl,
            double rating,
            int reviewCount,
            BigDecimal startingPrice,
            Set<String> amenities,
            ScoreBreakdown score
    ) {}

    public record SearchResponse(
            List<HotelCardResponse> hotels,
            int page,
            int size,
            int totalElements,
            int totalPages,
            boolean cached,
            long elapsedMs
    ) {}

    public record RoomResponse(
            Long id,
            String roomType,
            BigDecimal pricePerNight,
            int capacity,
            boolean available
    ) {}

    public record ReviewResponse(
            Long id,
            String userName,
            int rating,
            String comment,
            Instant createdAt
    ) {}

    public record HotelDetailsResponse(
            Long id,
            String name,
            String city,
            String area,
            String description,
            String imageUrl,
            double rating,
            int reviewCount,
            Set<String> amenities,
            List<RoomResponse> rooms,
            List<ReviewResponse> reviews
    ) {}

    public record AutocompleteResponse(List<String> suggestions) {}
}
