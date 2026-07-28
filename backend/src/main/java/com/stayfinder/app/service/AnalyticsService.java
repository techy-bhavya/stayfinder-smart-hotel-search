package com.stayfinder.app.service;

import com.stayfinder.app.dto.AnalyticsDtos.*;
import com.stayfinder.app.model.Booking;
import com.stayfinder.app.model.BookingStatus;
import com.stayfinder.app.repository.BookingRepository;
import com.stayfinder.app.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public AnalyticsService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse overview() {
        List<Booking> all = bookingRepository.findAllByOrderByCreatedAtDesc();
        List<Booking> confirmed = all.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        BigDecimal revenue = confirmed.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cancelled = all.stream().filter(booking -> booking.getStatus() == BookingStatus.CANCELLED).count();
        double cancellationRate = all.isEmpty() ? 0 : round((double) cancelled * 100 / all.size());
        double occupancy = calculateOccupancy(confirmed);

        Map<String, Long> bookingsByCity = confirmed.stream().collect(Collectors.groupingBy(
                booking -> booking.getRoom().getHotel().getCity(), Collectors.counting()
        ));
        String topCity = bookingsByCity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        Kpis kpis = new Kpis(revenue, confirmed.size(), occupancy, cancellationRate, topCity);
        return new AnalyticsResponse(kpis, monthlyRevenue(confirmed), cityPerformance(confirmed),
                propertyPerformance(confirmed));
    }

    private double calculateOccupancy(List<Booking> bookings) {
        LocalDate end = LocalDate.now().plusDays(1);
        LocalDate start = end.minusDays(30);
        long bookedRoomNights = 0;
        for (Booking booking : bookings) {
            LocalDate overlapStart = booking.getCheckIn().isAfter(start) ? booking.getCheckIn() : start;
            LocalDate overlapEnd = booking.getCheckOut().isBefore(end) ? booking.getCheckOut() : end;
            if (overlapEnd.isAfter(overlapStart)) {
                bookedRoomNights += ChronoUnit.DAYS.between(overlapStart, overlapEnd);
            }
        }
        long capacity = roomRepository.countByActiveTrue() * 30;
        return capacity == 0 ? 0 : round((double) bookedRoomNights * 100 / capacity);
    }

    private List<MonthlyRevenue> monthlyRevenue(List<Booking> bookings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<MonthlyRevenue> result = new ArrayList<>();
        YearMonth current = YearMonth.now().minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth month = current.plusMonths(i);
            List<Booking> matching = bookings.stream()
                    .filter(booking -> YearMonth.from(booking.getCheckIn()).equals(month))
                    .toList();
            BigDecimal revenue = matching.stream().map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(new MonthlyRevenue(month.format(formatter), revenue, matching.size()));
        }
        return result;
    }

    private List<CityPerformance> cityPerformance(List<Booking> bookings) {
        Map<String, List<Booking>> grouped = bookings.stream().collect(Collectors.groupingBy(
                booking -> booking.getRoom().getHotel().getCity()
        ));
        return grouped.entrySet().stream().map(entry -> {
                    BigDecimal revenue = entry.getValue().stream().map(Booking::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long count = entry.getValue().size();
                    BigDecimal average = count == 0 ? BigDecimal.ZERO
                            : revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                    return new CityPerformance(entry.getKey(), revenue, count, average.doubleValue());
                })
                .sorted(Comparator.comparing(CityPerformance::revenue).reversed())
                .toList();
    }

    private List<PropertyPerformance> propertyPerformance(List<Booking> bookings) {
        record HotelKey(Long id, String name, String city) {}
        Map<HotelKey, List<Booking>> grouped = bookings.stream().collect(Collectors.groupingBy(booking -> {
            var hotel = booking.getRoom().getHotel();
            return new HotelKey(hotel.getId(), hotel.getName(), hotel.getCity());
        }));

        return grouped.entrySet().stream().map(entry -> {
                    BigDecimal revenue = entry.getValue().stream().map(Booking::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    HotelKey key = entry.getKey();
                    return new PropertyPerformance(key.id(), key.name(), key.city(), revenue, entry.getValue().size());
                })
                .sorted(Comparator.comparing(PropertyPerformance::revenue).reversed())
                .limit(5)
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
