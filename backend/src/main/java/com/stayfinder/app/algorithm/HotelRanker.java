package com.stayfinder.app.algorithm;

import com.stayfinder.app.dto.HotelDtos.ScoreBreakdown;
import com.stayfinder.app.dto.HotelDtos.SearchCriteria;
import com.stayfinder.app.model.Hotel;

import java.math.BigDecimal;
import java.util.*;

/**
 * Explainable weighted ranking plus a min-heap for top-K selection.
 * Top-K complexity: O(n log K), compared with O(n log n) full sorting.
 */
public class HotelRanker {

    public record RankedHotel(Hotel hotel, BigDecimal startingPrice, ScoreBreakdown score) {}

    public List<RankedHotel> topK(List<Hotel> hotels, SearchCriteria criteria, int k) {
        if (k <= 0) {
            return List.of();
        }

        PriorityQueue<RankedHotel> minHeap = new PriorityQueue<>(
                Comparator.comparingDouble(item -> item.score().total())
        );

        for (Hotel hotel : hotels) {
            BigDecimal startingPrice = hotel.getRooms().stream()
                    .filter(room -> Boolean.TRUE.equals(room.getActive()))
                    .map(room -> room.getPricePerNight())
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            RankedHotel ranked = new RankedHotel(hotel, startingPrice, score(hotel, startingPrice, criteria));
            if (minHeap.size() < k) {
                minHeap.offer(ranked);
            } else if (ranked.score().total() > Objects.requireNonNull(minHeap.peek()).score().total()) {
                minHeap.poll();
                minHeap.offer(ranked);
            }
        }

        List<RankedHotel> result = new ArrayList<>(minHeap);
        result.sort(Comparator.comparingDouble((RankedHotel item) -> item.score().total()).reversed());
        return result;
    }

    private ScoreBreakdown score(Hotel hotel, BigDecimal startingPrice, SearchCriteria criteria) {
        double text = textScore(hotel, criteria.query(), criteria.city());
        double rating = clamp(hotel.getRating() / 5.0) * 25.0;
        double price = priceScore(startingPrice, criteria.maxPrice()) * 15.0;
        double amenities = amenityScore(hotel.getAmenities(), criteria.amenities()) * 15.0;
        double popularity = Math.min(Math.log10(Math.max(1, hotel.getReviewCount()) + 1) / 3.0, 1.0) * 10.0;
        double total = round(text + rating + price + amenities + popularity);
        return new ScoreBreakdown(total, round(text), round(rating), round(price), round(amenities), round(popularity));
    }

    private double textScore(Hotel hotel, String query, String city) {
        double score = 0;
        String name = hotel.getName().toLowerCase(Locale.ROOT);
        String hotelCity = hotel.getCity().toLowerCase(Locale.ROOT);
        String area = hotel.getArea().toLowerCase(Locale.ROOT);

        if (city != null && !city.isBlank()) {
            String expectedCity = city.trim().toLowerCase(Locale.ROOT);
            if (hotelCity.equals(expectedCity)) {
                score += 15;
            } else if (hotelCity.contains(expectedCity)) {
                score += 8;
            }
        }

        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase(Locale.ROOT);
            if (name.equals(q)) score += 20;
            else if (name.startsWith(q)) score += 16;
            else if (name.contains(q)) score += 12;
            if (hotelCity.contains(q)) score += 10;
            if (area.contains(q)) score += 6;
        } else {
            score += 15;
        }
        return Math.min(score, 35);
    }

    private double priceScore(BigDecimal price, BigDecimal maxPrice) {
        if (price == null || price.signum() <= 0) return 0;
        if (maxPrice == null || maxPrice.signum() <= 0) {
            return 1.0 / (1.0 + price.doubleValue() / 10_000.0);
        }
        double ratio = price.doubleValue() / maxPrice.doubleValue();
        return clamp(1.25 - ratio);
    }

    private double amenityScore(Set<String> hotelAmenities, Set<String> requested) {
        if (requested == null || requested.isEmpty()) return 1.0;
        Set<String> normalized = new HashSet<>();
        hotelAmenities.forEach(item -> normalized.add(item.toLowerCase(Locale.ROOT)));
        long matched = requested.stream()
                .map(item -> item.toLowerCase(Locale.ROOT))
                .filter(normalized::contains)
                .count();
        return (double) matched / requested.size();
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
