package com.stayfinder.app.service;

import com.stayfinder.app.dto.BookingDtos.BookingRequest;
import com.stayfinder.app.dto.BookingDtos.BookingResponse;
import com.stayfinder.app.exception.ApiException;
import com.stayfinder.app.model.*;
import com.stayfinder.app.repository.BookingRepository;
import com.stayfinder.app.repository.RoomRepository;
import com.stayfinder.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final HotelService hotelService;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          UserRepository userRepository, HotelService hotelService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.hotelService = hotelService;
    }

    @Transactional
    public BookingResponse create(String email, BookingRequest request) {
        validateDates(request.checkIn(), request.checkOut());
        User user = requireUser(email);
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!Boolean.TRUE.equals(room.getActive()) || !Boolean.TRUE.equals(room.getHotel().getActive())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This room is not available for booking");
        }
        if (request.guests() > room.getCapacity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This room supports a maximum of " + room.getCapacity() + " guests");
        }
        if (bookingRepository.existsOverlappingBooking(room.getId(), request.checkIn(), request.checkOut())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "The room was just booked for these dates. Please choose another room or date range.");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .guests(request.guests())
                .totalAmount(total)
                .status(BookingStatus.CONFIRMED)
                .build();
        bookingRepository.save(booking);
        hotelService.clearCache();
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(String email) {
        User user = requireUser(email);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancel(String email, Long bookingId) {
        User user = requireUser(email);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean owner = booking.getUser().getId().equals(user.getId());
        boolean admin = user.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel another user's booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Booking is already cancelled");
        }
        if (!booking.getCheckIn().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Past or active stays cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        hotelService.clearCache();
        return toResponse(booking);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in cannot be in the past");
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User account not found"));
    }

    private BookingResponse toResponse(Booking booking) {
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        return new BookingResponse(
                booking.getId(), hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getImageUrl(),
                room.getId(), room.getRoomType(), booking.getCheckIn(), booking.getCheckOut(),
                booking.getGuests(), nights, booking.getTotalAmount(), booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }
}
