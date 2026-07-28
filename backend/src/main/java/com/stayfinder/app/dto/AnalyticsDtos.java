package com.stayfinder.app.dto;

import java.math.BigDecimal;
import java.util.List;

public final class AnalyticsDtos {
    private AnalyticsDtos() {}

    public record Kpis(
            BigDecimal totalRevenue,
            long confirmedBookings,
            double occupancyRate,
            double cancellationRate,
            String topCity
    ) {}

    public record MonthlyRevenue(String month, BigDecimal revenue, long bookings) {}

    public record CityPerformance(
            String city,
            BigDecimal revenue,
            long bookings,
            double averageBookingValue
    ) {}

    public record PropertyPerformance(
            Long hotelId,
            String hotelName,
            String city,
            BigDecimal revenue,
            long bookings
    ) {}

    public record AnalyticsResponse(
            Kpis kpis,
            List<MonthlyRevenue> monthlyRevenue,
            List<CityPerformance> cityPerformance,
            List<PropertyPerformance> topProperties
    ) {}
}
