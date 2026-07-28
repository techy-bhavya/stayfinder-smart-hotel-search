package com.stayfinder.app.controller;

import com.stayfinder.app.dto.BookingDtos.BookingRequest;
import com.stayfinder.app.dto.BookingDtos.BookingResponse;
import com.stayfinder.app.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request, Authentication authentication) {
        return bookingService.create(authentication.getName(), request);
    }

    @GetMapping("/me")
    public List<BookingResponse> mine(Authentication authentication) {
        return bookingService.myBookings(authentication.getName());
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, Authentication authentication) {
        return bookingService.cancel(authentication.getName(), id);
    }
}
