package com.stayfinder.app.service;

import com.stayfinder.app.algorithm.HotelRanker;
import com.stayfinder.app.algorithm.LruCache;
import com.stayfinder.app.algorithm.TrieAutocomplete;
import com.stayfinder.app.dto.HotelDtos.*;
import com.stayfinder.app.exception.ApiException;
import com.stayfinder.app.model.Hotel;
import com.stayfinder.app.model.Room;
import com.stayfinder.app.repository.BookingRepository;
import com.stayfinder.app.repository.HotelRepository;
import com.stayfinder.app.repository.ReviewRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final TrieAutocomplete trie = new TrieAutocomplete();
    private final HotelRanker ranker = new HotelRanker();
    private final LruCache<String, CachedSearch> cache;

    public HotelService(HotelRepository hotelRepository,
                        BookingRepository bookingRepository,
                        ReviewRepository reviewRepository,
                        @Value("${app.search.cache-size}") int cacheSize) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.cache = new LruCache<>(cacheSize);
    }

    @PostConstruct
    @Transactional(readOnly = true)
    public void rebuildSearchIndex() {
        trie.clear();
        hotelRepository.findByActiveTrue().forEach(hotel -> {
            trie.insert(hotel.getName());
            trie.insert(hotel.getCity());
            trie.insert(hotel.getArea());
        });
    }

    public AutocompleteResponse autocomplete(String query) {
        return new AutocompleteResponse(trie.suggest(query, 8));
    }

    @Transactional(readOnly = true)
    public SearchResponse search(SearchCriteria rawCriteria) {
        long started = System.nanoTime();
        SearchCriteria criteria = normalize(rawCriteria);
        String key = cacheKey(criteria);

        Optional<CachedSearch> cached = cache.get(key);
        if (cached.isPresent()) {
            CachedSearch entry = cached.get();
            return new SearchResponse(entry.hotels(), criteria.page(), criteria.size(),
                    entry.totalElements(), entry.totalPages(), true, elapsedMs(started));
        }

        List<Hotel> filtered = hotelRepository.findByActiveTrue().stream()
                .filter(hotel -> matches(hotel, criteria))
                .toList();

        int totalElements = filtered.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / criteria.size());
        int from = Math.min(criteria.page() * criteria.size(), totalElements);
        int to = Math.min(from + criteria.size(), totalElements);

        List<HotelCardResponse> page = List.of();
        if (from < to) {
            List<HotelRanker.RankedHotel> ranked = ranker.topK(filtered, criteria, to);
            page = ranked.subList(from, to).stream().map(this::toCard).toList();
        }

        cache.put(key, new CachedSearch(page, totalElements, totalPages));
        return new SearchResponse(page, criteria.page(), criteria.size(), totalElements,
                totalPages, false, elapsedMs(started));
    }

    @Transactional(readOnly = true)
    public HotelDetailsResponse details(Long id, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut, false);
        Hotel hotel = hotelRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hotel not found"));

        List<RoomResponse> rooms = hotel.getRooms().stream()
                .filter(room -> Boolean.TRUE.equals(room.getActive()))
                .sorted(Comparator.comparing(Room::getPricePerNight))
                .map(room -> new RoomResponse(
                        room.getId(), room.getRoomType(), room.getPricePerNight(), room.getCapacity(),
                        isRoomAvailable(room.getId(), checkIn, checkOut)
                ))
                .toList();

        List<ReviewResponse> reviews = reviewRepository.findByHotelIdOrderByCreatedAtDesc(id).stream()
                .map(review -> new ReviewResponse(
                        review.getId(), review.getUser().getName(), review.getRating(),
                        review.getComment(), review.getCreatedAt()
                ))
                .toList();

        return new HotelDetailsResponse(
                hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getArea(), hotel.getDescription(),
                hotel.getImageUrl(), hotel.getRating(), hotel.getReviewCount(), hotel.getAmenities(), rooms, reviews
        );
    }

    public void clearCache() {
        cache.clear();
    }

    public void refreshIndexAndCache() {
        rebuildSearchIndex();
        clearCache();
    }

    private SearchCriteria normalize(SearchCriteria criteria) {
        int page = Math.max(0, criteria.page());
        int size = Math.min(24, Math.max(1, criteria.size()));
        Set<String> amenities = criteria.amenities() == null ? Set.of() : criteria.amenities().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
        validateDates(criteria.checkIn(), criteria.checkOut(), false);
        if (criteria.minPrice() != null && criteria.maxPrice() != null
                && criteria.minPrice().compareTo(criteria.maxPrice()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Minimum price cannot exceed maximum price");
        }
        return new SearchCriteria(
                trimToNull(criteria.query()), trimToNull(criteria.city()), criteria.minPrice(), criteria.maxPrice(),
                criteria.minRating(), amenities, criteria.checkIn(), criteria.checkOut(), page, size
        );
    }

    private boolean matches(Hotel hotel, SearchCriteria criteria) {
        String query = criteria.query() == null ? null : criteria.query().toLowerCase(Locale.ROOT);
        String city = criteria.city() == null ? null : criteria.city().toLowerCase(Locale.ROOT);

        if (query != null) {
            String searchable = (hotel.getName() + " " + hotel.getCity() + " " + hotel.getArea())
                    .toLowerCase(Locale.ROOT);
            if (!searchable.contains(query)) return false;
        }
        if (city != null && !hotel.getCity().toLowerCase(Locale.ROOT).contains(city)) return false;
        if (criteria.minRating() != null && hotel.getRating() < criteria.minRating()) return false;

        BigDecimal startingPrice = hotel.getRooms().stream()
                .filter(room -> Boolean.TRUE.equals(room.getActive()))
                .map(Room::getPricePerNight)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        if (criteria.minPrice() != null && startingPrice.compareTo(criteria.minPrice()) < 0) return false;
        if (criteria.maxPrice() != null && startingPrice.compareTo(criteria.maxPrice()) > 0) return false;

        Set<String> normalizedAmenities = hotel.getAmenities().stream()
                .map(item -> item.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        boolean amenitiesMatch = criteria.amenities().stream()
                .map(item -> item.toLowerCase(Locale.ROOT))
                .allMatch(normalizedAmenities::contains);
        if (!amenitiesMatch) return false;

        if (criteria.checkIn() != null && criteria.checkOut() != null) {
            return hotel.getRooms().stream()
                    .filter(room -> Boolean.TRUE.equals(room.getActive()))
                    .anyMatch(room -> isRoomAvailable(room.getId(), criteria.checkIn(), criteria.checkOut()));
        }
        return true;
    }

    private boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return true;
        return !bookingRepository.existsOverlappingBooking(roomId, checkIn, checkOut);
    }

    private HotelCardResponse toCard(HotelRanker.RankedHotel item) {
        Hotel hotel = item.hotel();
        return new HotelCardResponse(
                hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getArea(), hotel.getImageUrl(),
                hotel.getRating(), hotel.getReviewCount(), item.startingPrice(), hotel.getAmenities(), item.score()
        );
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut, boolean required) {
        if (required && (checkIn == null || checkOut == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in and check-out dates are required");
        }
        if ((checkIn == null) != (checkOut == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Provide both check-in and check-out dates");
        }
        if (checkIn != null && !checkOut.isAfter(checkIn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
    }

    private String cacheKey(SearchCriteria criteria) {
        return String.join("|",
                Objects.toString(criteria.query(), ""), Objects.toString(criteria.city(), ""),
                Objects.toString(criteria.minPrice(), ""), Objects.toString(criteria.maxPrice(), ""),
                Objects.toString(criteria.minRating(), ""), String.join(",", criteria.amenities()),
                Objects.toString(criteria.checkIn(), ""), Objects.toString(criteria.checkOut(), ""),
                String.valueOf(criteria.page()), String.valueOf(criteria.size())
        ).toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private record CachedSearch(List<HotelCardResponse> hotels, int totalElements, int totalPages) {}
}
