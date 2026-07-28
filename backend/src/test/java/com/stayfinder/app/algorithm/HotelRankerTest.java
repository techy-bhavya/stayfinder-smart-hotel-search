package com.stayfinder.app.algorithm;

import com.stayfinder.app.dto.HotelDtos.SearchCriteria;
import com.stayfinder.app.model.Hotel;
import com.stayfinder.app.model.Room;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HotelRankerTest {
    @Test
    void usesHeapToReturnHighestScoringTopK() {
        Hotel jaipur = hotel("Amber Jaipur", "Jaipur", 4.9, 220, 5000, Set.of("Pool", "WiFi"));
        Hotel goa = hotel("Coast House", "Goa", 4.6, 180, 7000, Set.of("Pool", "WiFi"));
        Hotel delhi = hotel("Metro Stay", "Delhi", 4.2, 40, 4000, Set.of("WiFi"));
        SearchCriteria criteria = new SearchCriteria(
                "Jaipur", "Jaipur", null, BigDecimal.valueOf(8000), 4.0,
                Set.of("Pool"), null, null, 0, 10
        );

        List<HotelRanker.RankedHotel> result = new HotelRanker().topK(List.of(delhi, goa, jaipur), criteria, 2);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().hotel().getName()).isEqualTo("Amber Jaipur");
        assertThat(result.getFirst().score().total()).isGreaterThan(result.get(1).score().total());
    }

    private Hotel hotel(String name, String city, double rating, int reviews, int price, Set<String> amenities) {
        Hotel hotel = Hotel.builder()
                .name(name).city(city).area("Central").description("Test")
                .imageUrl("https://example.com/image.jpg").rating(rating).reviewCount(reviews)
                .active(true).amenities(new LinkedHashSet<>(amenities)).build();
        hotel.addRoom(Room.builder().roomType("Standard").pricePerNight(BigDecimal.valueOf(price))
                .capacity(2).active(true).build());
        return hotel;
    }
}
