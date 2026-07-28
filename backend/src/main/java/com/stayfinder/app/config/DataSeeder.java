package com.stayfinder.app.config;

import com.stayfinder.app.model.*;
import com.stayfinder.app.repository.*;
import com.stayfinder.app.service.HotelService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Order(10)
public class DataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelService hotelService;

    public DataSeeder(UserRepository userRepository, HotelRepository hotelRepository,
                      ReviewRepository reviewRepository, BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder, HotelService hotelService) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.hotelService = hotelService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hotelRepository.count() > 0) {
            hotelService.refreshIndexAndCache();
            return;
        }

        User admin = saveUser("Aarav Admin", "admin@stayfinder.dev", "Admin@123", Role.ADMIN);
        User demo = saveUser("Riya Sharma", "demo@stayfinder.dev", "Demo@123", Role.USER);
        User second = saveUser("Kabir Mehta", "kabir@stayfinder.dev", "Demo@123", Role.USER);

        List<Hotel> hotels = hotelRepository.saveAll(List.of(
                hotel("Amber Courtyard", "Jaipur", "Bani Park",
                        "A warm heritage-inspired stay with sandstone courtyards, contemporary rooms and quick access to Jaipur's old city.",
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Pool", "Breakfast", "WiFi", "Parking", "Heritage"),
                        room("Deluxe King", 4200, 2), room("Courtyard Suite", 6800, 3), room("Family Room", 7900, 4)),
                hotel("Skyline Residency", "Gurugram", "Cyber City",
                        "A business-first property with fast WiFi, work-friendly rooms, airport transfers and skyline views near major offices.",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1400&q=85",
                        Set.of("WiFi", "Gym", "Breakfast", "Workspace", "Airport Transfer"),
                        room("Business Queen", 5100, 2), room("Executive King", 7100, 2), room("Skyline Suite", 9800, 3)),
                hotel("Bay & Bloom", "Mumbai", "Bandra West",
                        "A design-led urban retreat combining coastal colours, chef-led dining and easy access to Bandra's cafés and nightlife.",
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Pool", "WiFi", "Breakfast", "Restaurant", "Pet Friendly"),
                        room("Studio Queen", 6300, 2), room("Bay View King", 8900, 2), room("Bloom Suite", 12500, 3)),
                hotel("Cedar House", "Manali", "Old Manali",
                        "A quiet mountain house with cedar interiors, valley-facing balconies and locally sourced breakfast.",
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Mountain View", "Breakfast", "WiFi", "Bonfire", "Parking"),
                        room("Cedar Room", 3600, 2), room("Valley Balcony", 5200, 2), room("Family Chalet", 7600, 4)),
                hotel("Marina Eight", "Goa", "Morjim",
                        "A relaxed beachside stay with tropical gardens, an outdoor pool and spacious rooms within walking distance of Morjim beach.",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Pool", "Beach Access", "Breakfast", "WiFi", "Bar"),
                        room("Garden Room", 4700, 2), room("Poolside King", 6900, 2), room("Marina Villa", 11800, 4)),
                hotel("The Residency 27", "Bengaluru", "Indiranagar",
                        "A polished city hotel for work and weekend trips, featuring modern rooms, coworking lounges and late-night dining.",
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Workspace", "Gym", "WiFi", "Breakfast", "Restaurant"),
                        room("Smart Queen", 4600, 2), room("Club King", 6200, 2), room("Residency Suite", 9100, 3)),
                hotel("Ganga Atelier", "Rishikesh", "Tapovan",
                        "A calm wellness retreat with yoga sessions, river-inspired interiors and plant-forward meals near Tapovan.",
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Yoga", "Breakfast", "WiFi", "River View", "Spa"),
                        room("Atelier Room", 3900, 2), room("River View Room", 5900, 2), room("Wellness Suite", 8400, 3)),
                hotel("Park Street Social", "Kolkata", "Park Street",
                        "A lively central stay with art-filled interiors, excellent food and effortless access to Kolkata's cultural landmarks.",
                        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Restaurant", "Breakfast", "WiFi", "Gym", "City Centre"),
                        room("Classic Queen", 4400, 2), room("Social King", 6100, 2), room("Park Suite", 8700, 3)),
                hotel("Lakehouse Udaipur", "Udaipur", "Fateh Sagar",
                        "A romantic lakeside property with arched terraces, sunset dining and elegant rooms influenced by Mewar craftsmanship.",
                        "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1400&q=85",
                        Set.of("Lake View", "Pool", "Breakfast", "Restaurant", "Spa"),
                        room("Heritage Room", 5800, 2), room("Lake View King", 8600, 2), room("Mewar Suite", 13200, 3))
        ));

        seedReviews(hotels, demo, second, admin);
        seedBookings(hotels, demo, second);
        hotelService.refreshIndexAndCache();
    }

    private User saveUser(String name, String email, String password, Role role) {
        return userRepository.save(User.builder()
                .name(name).email(email).password(passwordEncoder.encode(password)).role(role).build());
    }

    private Hotel hotel(String name, String city, String area, String description, String image,
                        Set<String> amenities, Room... rooms) {
        Hotel hotel = Hotel.builder()
                .name(name).city(city).area(area).description(description).imageUrl(image)
                .rating(4.5).reviewCount(0).active(true)
                .amenities(new LinkedHashSet<>(amenities)).build();
        Arrays.stream(rooms).forEach(hotel::addRoom);
        return hotel;
    }

    private Room room(String type, int price, int capacity) {
        return Room.builder().roomType(type).pricePerNight(BigDecimal.valueOf(price))
                .capacity(capacity).active(true).build();
    }

    private void seedReviews(List<Hotel> hotels, User demo, User second, User admin) {
        String[] comments = {
                "Excellent location and a very smooth check-in experience.",
                "The rooms were spotless, staff were helpful and breakfast was genuinely good.",
                "Loved the design and amenities. I would happily stay here again."
        };
        for (int i = 0; i < hotels.size(); i++) {
            Hotel hotel = hotels.get(i);
            int firstRating = 4 + (i % 2);
            int secondRating = i % 3 == 0 ? 4 : 5;
            reviewRepository.save(Review.builder().user(demo).hotel(hotel).rating(firstRating)
                    .comment(comments[i % comments.length]).createdAt(Instant.now().minusSeconds((long) (i + 2) * 86400)).build());
            reviewRepository.save(Review.builder().user(second).hotel(hotel).rating(secondRating)
                    .comment(comments[(i + 1) % comments.length]).createdAt(Instant.now().minusSeconds((long) (i + 5) * 86400)).build());
            if (i % 2 == 0) {
                reviewRepository.save(Review.builder().user(admin).hotel(hotel).rating(5)
                        .comment(comments[(i + 2) % comments.length]).createdAt(Instant.now().minusSeconds((long) (i + 9) * 86400)).build());
            }
            List<Review> reviews = reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotel.getId());
            double average = reviews.stream().mapToInt(Review::getRating).average().orElse(4.5);
            hotel.setRating(Math.round(average * 10.0) / 10.0);
            hotel.setReviewCount(40 + i * 17);
            hotelRepository.save(hotel);
        }
    }

    private void seedBookings(List<Hotel> hotels, User demo, User second) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 28; i++) {
            Hotel hotel = hotels.get(i % hotels.size());
            Room room = hotel.getRooms().get(i % hotel.getRooms().size());
            LocalDate checkIn = today.minusMonths(5).plusDays(i * 7L);
            LocalDate checkOut = checkIn.plusDays(2 + i % 4);
            BookingStatus status = i % 8 == 0 ? BookingStatus.CANCELLED : BookingStatus.CONFIRMED;
            User guest = i % 2 == 0 ? demo : second;
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            Booking booking = Booking.builder()
                    .user(guest).room(room).checkIn(checkIn).checkOut(checkOut)
                    .guests(Math.min(room.getCapacity(), 1 + i % 3))
                    .totalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)))
                    .status(status)
                    .createdAt(checkIn.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().minusSeconds(86400 * 5L))
                    .build();
            bookingRepository.save(booking);
        }

        // Future bookings make the availability and cancellation flows immediately demonstrable.
        for (int i = 0; i < 5; i++) {
            Hotel hotel = hotels.get(i);
            Room room = hotel.getRooms().get(0);
            LocalDate checkIn = today.plusDays(10L + i * 4L);
            LocalDate checkOut = checkIn.plusDays(3);
            bookingRepository.save(Booking.builder()
                    .user(i % 2 == 0 ? demo : second).room(room)
                    .checkIn(checkIn).checkOut(checkOut).guests(2)
                    .totalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(3)))
                    .status(BookingStatus.CONFIRMED).build());
        }
    }
}
