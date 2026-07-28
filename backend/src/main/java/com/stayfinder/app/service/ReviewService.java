package com.stayfinder.app.service;

import com.stayfinder.app.dto.HotelDtos.ReviewResponse;
import com.stayfinder.app.dto.ReviewDtos.ReviewRequest;
import com.stayfinder.app.exception.ApiException;
import com.stayfinder.app.model.Hotel;
import com.stayfinder.app.model.Review;
import com.stayfinder.app.model.User;
import com.stayfinder.app.repository.HotelRepository;
import com.stayfinder.app.repository.ReviewRepository;
import com.stayfinder.app.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final HotelService hotelService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         HotelRepository hotelRepository, HotelService hotelService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.hotelService = hotelService;
    }

    @Transactional
    public ReviewResponse upsert(String email, Long hotelId, ReviewRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User account not found"));
        Hotel hotel = hotelRepository.findByIdAndActiveTrue(hotelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hotel not found"));

        Review review = reviewRepository.findByUserIdAndHotelId(user.getId(), hotelId)
                .orElseGet(() -> Review.builder().user(user).hotel(hotel).build());
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        reviewRepository.save(review);
        recalculateHotelRating(hotel);
        hotelService.clearCache();
        return new ReviewResponse(review.getId(), user.getName(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }

    private void recalculateHotelRating(Hotel hotel) {
        List<Review> reviews = reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotel.getId());
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        hotel.setRating(Math.round(average * 10.0) / 10.0);
        hotel.setReviewCount(reviews.size());
        hotelRepository.save(hotel);
    }
}
