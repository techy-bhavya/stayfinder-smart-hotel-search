import { ArrowRight, CalendarDays, Database, Filter, MapPin, Search, ShieldCheck, SlidersHorizontal, Sparkles, X } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { api, ApiError } from '../api/client';
import { EmptyView, ErrorView, LoadingView } from '../components/StatusView';
import { HotelCard } from '../components/HotelCard';
import type { SearchResponse } from '../types';
import { addDaysIso, todayIso } from '../utils/format';

const AMENITIES = ['Pool', 'Breakfast', 'WiFi', 'Gym', 'Parking', 'Spa', 'Workspace', 'Beach Access'];

interface Filters {
  query: string;
  city: string;
  checkIn: string;
  checkOut: string;
  maxPrice: string;
  minRating: string;
  amenities: string[];
}

const initialFilters: Filters = {
  query: '',
  city: '',
  checkIn: addDaysIso(7),
  checkOut: addDaysIso(10),
  maxPrice: '10000',
  minRating: '0',
  amenities: [],
};

export function HomePage() {
  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [applied, setApplied] = useState<Filters>(initialFilters);
  const [data, setData] = useState<SearchResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [page, setPage] = useState(0);

  const params = useMemo(() => {
    const search = new URLSearchParams({ page: String(page), size: '9' });
    if (applied.query) search.set('query', applied.query);
    if (applied.city) search.set('city', applied.city);
    if (applied.checkIn) search.set('checkIn', applied.checkIn);
    if (applied.checkOut) search.set('checkOut', applied.checkOut);
    if (applied.maxPrice) search.set('maxPrice', applied.maxPrice);
    if (Number(applied.minRating) > 0) search.set('minRating', applied.minRating);
    if (applied.amenities.length) search.set('amenities', applied.amenities.join(','));
    return search.toString();
  }, [applied, page]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api<SearchResponse>(`/hotels/search?${params}`)
      .then((response) => active && setData(response))
      .catch((reason: ApiError) => active && setError(reason.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [params]);

  useEffect(() => {
    if (filters.query.trim().length < 2) {
      setSuggestions([]);
      return;
    }
    const timer = window.setTimeout(() => {
      api<{ suggestions: string[] }>(`/hotels/autocomplete?q=${encodeURIComponent(filters.query)}`)
        .then((response) => setSuggestions(response.suggestions))
        .catch(() => setSuggestions([]));
    }, 180);
    return () => window.clearTimeout(timer);
  }, [filters.query]);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    setPage(0);
    setApplied(filters);
    setSuggestions([]);
  };

  const toggleAmenity = (amenity: string) => {
    setFilters((current) => ({
      ...current,
      amenities: current.amenities.includes(amenity)
        ? current.amenities.filter((item) => item !== amenity)
        : [...current.amenities, amenity],
    }));
  };

  const clearFilters = () => {
    setFilters(initialFilters);
    setApplied(initialFilters);
    setPage(0);
  };

  return (
    <main>
      <section className="hero-section">
        <div className="hero-glow hero-glow-one" />
        <div className="hero-glow hero-glow-two" />
        <div className="container hero-grid">
          <div className="hero-copy">
            <div className="eyebrow"><Sparkles size={16} /> Explainable hotel discovery</div>
            <h1>Search smarter.<br /><span>Stay better.</span></h1>
            <p>
              A full-stack hospitality platform that ranks stays with real algorithms—not random cards.
              Search, compare, book and understand every recommendation.
            </p>
            <div className="hero-trust-row">
              <span><ShieldCheck size={17} /> Secure booking</span>
              <span><Database size={17} /> Live availability</span>
              <span><SlidersHorizontal size={17} /> Explainable ranking</span>
            </div>
          </div>

          <div className="hero-stat-panel">
            <div className="floating-card floating-card-main">
              <span>Search engine</span>
              <strong>Trie + Heap + LRU</strong>
              <small>Fast suggestions, top-K ranking and cached results</small>
            </div>
            <div className="floating-card floating-card-a"><strong>O(n log K)</strong><span>Top-K search</span></div>
            <div className="floating-card floating-card-b"><strong>O(m)</strong><span>Autocomplete</span></div>
          </div>
        </div>

        <form className="search-console container" onSubmit={submit}>
          <div className="search-main-row">
            <label className="search-field grow-field">
              <span><Search size={16} /> Hotel, area or city</span>
              <input
                value={filters.query}
                onChange={(event) => setFilters({ ...filters, query: event.target.value })}
                placeholder="Try Jaipur, beach or Skyline"
              />
              {suggestions.length > 0 && (
                <div className="suggestions">
                  {suggestions.map((suggestion) => (
                    <button type="button" key={suggestion} onClick={() => {
                      setFilters({ ...filters, query: suggestion });
                      setSuggestions([]);
                    }}>
                      <MapPin size={15} /> {suggestion}
                    </button>
                  ))}
                </div>
              )}
            </label>
            <label className="search-field">
              <span><MapPin size={16} /> City</span>
              <input value={filters.city} onChange={(event) => setFilters({ ...filters, city: event.target.value })} placeholder="Any city" />
            </label>
            <label className="search-field">
              <span><CalendarDays size={16} /> Check-in</span>
              <input type="date" min={todayIso()} value={filters.checkIn} onChange={(event) => setFilters({ ...filters, checkIn: event.target.value })} />
            </label>
            <label className="search-field">
              <span><CalendarDays size={16} /> Check-out</span>
              <input type="date" min={filters.checkIn || todayIso()} value={filters.checkOut} onChange={(event) => setFilters({ ...filters, checkOut: event.target.value })} />
            </label>
            <button className="button search-button" type="submit">Explore <ArrowRight size={18} /></button>
          </div>

          <div className="search-advanced-toggle-row">
            <button type="button" className="text-button" onClick={() => setShowAdvanced(!showAdvanced)}>
              <Filter size={16} /> {showAdvanced ? 'Hide filters' : 'More filters'}
            </button>
            <button type="button" className="text-button muted" onClick={clearFilters}><X size={15} /> Reset</button>
          </div>

          {showAdvanced && (
            <div className="advanced-filters">
              <label>
                <span>Maximum nightly price</span>
                <select value={filters.maxPrice} onChange={(event) => setFilters({ ...filters, maxPrice: event.target.value })}>
                  <option value="">Any budget</option>
                  <option value="5000">Up to ₹5,000</option>
                  <option value="7500">Up to ₹7,500</option>
                  <option value="10000">Up to ₹10,000</option>
                  <option value="15000">Up to ₹15,000</option>
                </select>
              </label>
              <label>
                <span>Minimum rating</span>
                <select value={filters.minRating} onChange={(event) => setFilters({ ...filters, minRating: event.target.value })}>
                  <option value="0">Any rating</option>
                  <option value="4">4.0+</option>
                  <option value="4.5">4.5+</option>
                  <option value="4.8">4.8+</option>
                </select>
              </label>
              <div className="amenity-filter-block">
                <span>Must-have amenities</span>
                <div className="amenity-checkboxes">
                  {AMENITIES.map((amenity) => (
                    <button
                      type="button"
                      key={amenity}
                      className={filters.amenities.includes(amenity) ? 'selected' : ''}
                      onClick={() => toggleAmenity(amenity)}
                    >{amenity}</button>
                  ))}
                </div>
              </div>
            </div>
          )}
        </form>
      </section>

      <section className="results-section container">
        <div className="section-heading">
          <div>
            <span className="section-kicker">Curated by algorithms</span>
            <h2>Stays matched to your search</h2>
          </div>
          {data && (
            <div className="query-metadata">
              <strong>{data.totalElements} properties</strong>
              <span>{data.cached ? 'LRU cache hit' : 'Fresh ranking'} · {data.elapsedMs} ms</span>
            </div>
          )}
        </div>

        {loading && <LoadingView />}
        {!loading && error && <ErrorView message={error} />}
        {!loading && !error && data?.hotels.length === 0 && <EmptyView />}
        {!loading && !error && data && data.hotels.length > 0 && (
          <>
            <div className="hotel-grid">
              {data.hotels.map((hotel) => (
                <HotelCard key={hotel.id} hotel={hotel} checkIn={applied.checkIn} checkOut={applied.checkOut} />
              ))}
            </div>
            {data.totalPages > 1 && (
              <div className="pagination">
                <button disabled={page === 0} onClick={() => setPage((current) => current - 1)}>Previous</button>
                <span>Page {page + 1} of {data.totalPages}</span>
                <button disabled={page + 1 >= data.totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
              </div>
            )}
          </>
        )}
      </section>
    </main>
  );
}
