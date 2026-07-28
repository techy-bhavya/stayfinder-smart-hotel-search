package com.stayfinder.app.controller;

import com.stayfinder.app.dto.HotelDtos.*;
import com.stayfinder.app.dto.ReviewDtos.ReviewRequest;
import com.stayfinder.app.service.HotelService;
import com.stayfinder.app.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hotels")
public class HotelController {
    private final HotelService hotelService;
    private final ReviewService reviewService;

    public HotelController(HotelService hotelService, ReviewService reviewService) {
        this.hotelService = hotelService;
        this.reviewService = reviewService;
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Set<String> amenitySet = amenities == null || amenities.isBlank()
                ? Set.of()
                : Arrays.stream(amenities.split(",")).map(String::trim).filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
        return hotelService.search(new SearchCriteria(
                query, city, minPrice, maxPrice, minRating, amenitySet, checkIn, checkOut, page, size
        ));
    }

    @GetMapping("/autocomplete")
    public AutocompleteResponse autocomplete(@RequestParam String q) {
        return hotelService.autocomplete(q);
    }

    @GetMapping("/{id}")
    public HotelDetailsResponse details(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return hotelService.details(id, checkIn, checkOut);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse review(@PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request,
                                 Authentication authentication) {
        return reviewService.upsert(authentication.getName(), id, request);
    }
}
