# StayFinder Interview Guide

## 1. Thirty-second introduction

> StayFinder is a full-stack hotel search and booking platform built with Java, Spring Boot, React, TypeScript and MySQL. I wanted the project to demonstrate both product engineering and core computer-science fundamentals. I implemented Trie-based autocomplete, a heap-based top-K ranking engine, an LRU cache, JWT authentication, room-level conflict prevention and a business analytics dashboard. The system is containerised with Docker and includes automated tests and CI.

## 2. Why did you choose this project?

It models a real hospitality technology problem rather than a generic CRUD application. It contains user-facing workflows, meaningful algorithms, relational data modelling, security, analytics and deployment concerns. It also creates multiple discussion points for DSA, OOP, DBMS, operating systems and system design interviews.

## 3. Explain the architecture

The React frontend calls a stateless Spring Boot REST API. JWT tokens identify users. Controllers validate the API contract and delegate to services. Services own business rules. Spring Data repositories handle persistence. MySQL stores users, hotels, rooms, bookings and reviews. Search has three specialised components: a Trie for prefix suggestions, a weighted scorer with a min-heap for top-K results and an LRU cache for repeated queries.

## 4. Why use a Trie for autocomplete?

A Trie stores strings character by character. Looking up a prefix of length `m` takes `O(m)` before collecting suggestions. A naive approach would scan every city and hotel name for every keystroke, taking roughly `O(n × m)`. The Trie is especially suitable because autocomplete is fundamentally a prefix-search problem.

### Cross-question: What is the drawback?

A Trie can consume more memory because every node stores child references. In a production system with a very large vocabulary, I could use a compressed Trie, radix tree, search engine such as Elasticsearch or a managed autocomplete service.

## 5. Why use a heap for ranking?

If there are `n` candidate hotels but the user needs only the best `K`, sorting every candidate costs `O(n log n)`. A min-heap of size `K` keeps only the current best results and costs `O(n log K)`. The smallest item stays at the root and is replaced when a better hotel appears.

### Cross-question: Why not always use the heap?

For a small dataset, full sorting is simpler and the performance difference is negligible. The heap is valuable when `n` is large and `K` is much smaller than `n`.

## 6. How does ranking work?

Each result receives a weighted score:

- Text and city relevance: up to 35 points
- Guest rating: up to 25 points
- Price value: up to 15 points
- Requested amenity match: up to 15 points
- Popularity: up to 10 points

The API returns the individual components, making the ranking explainable. The weights are business rules and can later be learned or A/B tested.

### Cross-question: Is this machine learning?

No. It is an explainable heuristic ranking model. I deliberately kept it deterministic for the first version. A future version could use click-through, conversion and cancellation data to train a learning-to-rank model.

## 7. How does the LRU cache work?

The cache is backed by `LinkedHashMap` in access-order mode. Every get moves the entry to the most-recently-used end. When capacity is exceeded, the eldest entry is removed. Average get and put are `O(1)`.

### Cross-question: Why is an in-memory cache insufficient in production?

Every application instance would have a different cache, data disappears during restart and memory is limited. A distributed cache such as Redis would be more appropriate for a horizontally scaled deployment.

## 8. How do you prevent double booking?

The overlap rule is:

```text
existing.checkIn < requested.checkOut
AND
existing.checkOut > requested.checkIn
```

The service obtains a pessimistic write lock on the room, checks for an overlapping confirmed booking and saves only if no conflict exists. The same availability check is performed when showing rooms, but the transaction-time check is the authoritative one.

### Cross-question: Why check twice?

The room may be available when the page loads but another user may book it before confirmation. This is a time-of-check versus time-of-use race. Rechecking inside the booking transaction prevents relying on stale UI state.

### Cross-question: Is pessimistic locking enough at massive scale?

It is correct for this relational design but can reduce throughput under heavy contention. Alternatives include serialisable transactions, database exclusion constraints where available, inventory counters with atomic updates, queues or a reservation-hold service with expiration.

## 9. Explain the database design

- One user can have many bookings and reviews.
- One hotel has many rooms and amenities.
- One room can have many non-overlapping bookings over time.
- One user can review a hotel once due to a composite unique constraint.
- Indexes support email lookup, city filtering, price filtering and room/date booking checks.

## 10. Why use DTOs instead of returning entities?

DTOs prevent leaking internal fields such as password hashes, avoid accidental lazy-loading during JSON serialisation, stabilise the API contract and allow the response shape to differ from the database model.

## 11. Explain JWT authentication

After successful login, the backend signs a JWT containing the user's subject and role. The frontend stores it and sends it in the `Authorization` header. A Spring Security filter validates the signature and expiry, loads the user and sets the security context. Passwords are never stored directly; BCrypt hashes them.

### Cross-question: What would you improve?

Use short-lived access tokens, secure HttpOnly cookies or carefully managed storage, refresh-token rotation, token revocation, rate limiting, MFA for administrators and managed secrets.

## 12. What OOP/design principles are visible?

- Single responsibility: controllers, services, repositories and algorithm classes have separate roles.
- Dependency inversion: services depend on repository interfaces.
- Encapsulation: business rules remain inside services.
- Composition: `HotelService` composes ranking, Trie and cache components.
- Strategy opportunity: ranking can be extracted behind an interface to support alternate ranking policies.

## 13. What testing did you add?

The repository includes focused unit tests for:

- Case-insensitive Trie prefix matching
- LRU eviction behaviour
- Heap-based top-K ranking order

Further tests should cover booking concurrency, security rules, controller validation and repository queries using Testcontainers.

## 14. How is occupancy calculated?

For the last 30 days, the service computes confirmed room-nights that overlap the window and divides them by active-room inventory multiplied by 30. This is more meaningful than simply dividing bookings by rooms because stays can have different lengths.

## 15. What would you build next?

1. Redis caching and cache expiry.
2. Flyway database migrations.
3. Testcontainers integration tests.
4. Payment and temporary reservation holds.
5. Search indexing through Elasticsearch/OpenSearch.
6. Image upload through object storage.
7. Observability with metrics, logs and traces.
8. A data pipeline for recommendation-model training.

## 16. Difficult interview cross-questions

### What happens if the cache contains stale availability?

Booking creation never trusts cached availability. The transaction performs the database overlap check again. Search cache is cleared whenever a booking or review changes. A production version would also use a short TTL and event-driven invalidation.

### Why is your current search not truly scalable to millions of hotels?

The MVP loads active hotels and applies ranking in the application. This makes the algorithm easy to demonstrate but is not the final large-scale architecture. At high scale I would push hard filters to indexed database/search-engine queries, retrieve a bounded candidate set and rank only those candidates.

### Why did you use MySQL?

The domain has strong relationships and transactional consistency requirements. Room reservation is a good fit for ACID transactions, constraints and indexed date-overlap queries.

### How would you support multiple rooms of the same type?

The current model represents each room inventory unit separately. At larger scale, I would model a room type plus daily inventory counts, then atomically decrement inventory for each date in the requested range.

### What is the time complexity of search?

Candidate filtering is currently `O(n)` in the application. Ranking the best `K` candidates is `O(n log K)`. Trie prefix traversal is `O(m)` plus the cost of collecting returned suggestions. Cache lookup is average `O(1)`.
