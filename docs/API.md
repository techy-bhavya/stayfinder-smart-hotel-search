# StayFinder API Guide

Base URL: `http://localhost:8080/api`

## Authentication

### Register

```http
POST /auth/register
Content-Type: application/json

{
  "name": "Your Name",
  "email": "you@example.com",
  "password": "StrongPass123"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "demo@stayfinder.dev",
  "password": "Demo@123"
}
```

Copy the returned token and send it as:

```http
Authorization: Bearer <token>
```

## Hotel search

```http
GET /hotels/search?query=Jaipur&maxPrice=8000&minRating=4&amenities=Pool,WiFi&checkIn=2026-08-10&checkOut=2026-08-13&page=0&size=9
```

The response includes an explainable score:

```json
{
  "score": {
    "total": 88.4,
    "textMatch": 35.0,
    "rating": 24.0,
    "priceValue": 11.2,
    "amenityMatch": 15.0,
    "popularity": 3.2
  }
}
```

## Autocomplete

```http
GET /hotels/autocomplete?q=jai
```

The backend uses a case-insensitive Trie and returns up to eight prefix matches.

## Hotel details and availability

```http
GET /hotels/1?checkIn=2026-08-10&checkOut=2026-08-13
```

Each room includes an `available` boolean calculated through an overlap query.

## Create booking

```http
POST /bookings
Authorization: Bearer <token>
Content-Type: application/json

{
  "roomId": 2,
  "checkIn": "2026-08-10",
  "checkOut": "2026-08-13",
  "guests": 2
}
```

The service takes a pessimistic database lock on the room, repeats the overlap check and then saves the booking. This reduces race conditions when two users try to reserve the same room simultaneously.

## My bookings

```http
GET /bookings/me
Authorization: Bearer <token>
```

## Cancel booking

```http
PATCH /bookings/42/cancel
Authorization: Bearer <token>
```

Only the booking owner or an administrator can cancel it. Active and past stays cannot be cancelled.

## Reviews

```http
POST /hotels/1/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 5,
  "comment": "Excellent stay and very smooth check-in."
}
```

A user has one review per hotel. Submitting again updates the existing review.

## Analytics

```http
GET /analytics/overview
Authorization: Bearer <admin-token>
```

This endpoint is restricted to `ADMIN` users and returns:

- Total confirmed revenue
- Confirmed booking count
- Last-30-day room-night occupancy
- Cancellation rate
- Top destination
- Six-month revenue trend
- City performance
- Top properties

## Error format

```json
{
  "timestamp": "2026-07-29T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "The room was just booked for these dates.",
  "path": "/api/bookings",
  "validationErrors": {}
}
```
