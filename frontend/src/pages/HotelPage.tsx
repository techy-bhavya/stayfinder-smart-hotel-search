import { ArrowLeft, BedDouble, CalendarDays, Check, MapPin, ShieldCheck, Star, Users, Wifi } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { ErrorView, LoadingView } from '../components/StatusView';
import { useAuth } from '../context/AuthContext';
import type { BookingData, HotelDetails } from '../types';
import { addDaysIso, formatMoney, todayIso } from '../utils/format';

export function HotelPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const [checkIn, setCheckIn] = useState(searchParams.get('checkIn') || addDaysIso(7));
  const [checkOut, setCheckOut] = useState(searchParams.get('checkOut') || addDaysIso(10));
  const [hotel, setHotel] = useState<HotelDetails | null>(null);
  const [selectedRoom, setSelectedRoom] = useState<number | null>(null);
  const [guests, setGuests] = useState(2);
  const [loading, setLoading] = useState(true);
  const [booking, setBooking] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api<HotelDetails>(`/hotels/${id}?checkIn=${checkIn}&checkOut=${checkOut}`)
      .then((response) => {
        if (!active) return;
        setHotel(response);
        const currentSelected = response.rooms.find((room) => room.id === selectedRoom && room.available);
        setSelectedRoom(currentSelected?.id || response.rooms.find((room) => room.available)?.id || null);
      })
      .catch((reason: ApiError) => active && setError(reason.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, checkIn, checkOut]);

  const room = hotel?.rooms.find((item) => item.id === selectedRoom);
  const nights = useMemo(() => {
    if (!checkIn || !checkOut) return 0;
    return Math.max(0, Math.round((new Date(checkOut).getTime() - new Date(checkIn).getTime()) / 86_400_000));
  }, [checkIn, checkOut]);

  const createBooking = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) {
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }
    if (!selectedRoom) return;
    setBooking(true);
    setError('');
    setNotice('');
    try {
      const response = await api<BookingData>('/bookings', {
        method: 'POST',
        body: JSON.stringify({ roomId: selectedRoom, checkIn, checkOut, guests }),
      }, user.token);
      setNotice(`Booking #${response.id} confirmed. Your ${response.nights}-night stay is ready.`);
      const refreshed = await api<HotelDetails>(`/hotels/${id}?checkIn=${checkIn}&checkOut=${checkOut}`);
      setHotel(refreshed);
      setSelectedRoom(refreshed.rooms.find((item) => item.available)?.id || null);
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Booking failed');
    } finally {
      setBooking(false);
    }
  };

  const submitReview = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) {
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }
    try {
      await api(`/hotels/${id}/reviews`, {
        method: 'POST',
        body: JSON.stringify({ rating: reviewRating, comment: reviewComment }),
      }, user.token);
      setReviewComment('');
      setNotice('Your review has been published.');
      const refreshed = await api<HotelDetails>(`/hotels/${id}?checkIn=${checkIn}&checkOut=${checkOut}`);
      setHotel(refreshed);
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Could not publish review');
    }
  };

  if (loading) return <main className="page-shell container"><LoadingView label="Preparing your stay…" /></main>;
  if (error && !hotel) return <main className="page-shell container"><ErrorView message={error} /></main>;
  if (!hotel) return null;

  return (
    <main className="detail-page">
      <section className="detail-hero">
        <img src={hotel.imageUrl} alt={hotel.name} />
        <div className="detail-overlay" />
        <div className="container detail-hero-content">
          <Link className="back-link light" to="/"><ArrowLeft size={17} /> Back to results</Link>
          <div className="detail-title-row">
            <div>
              <div className="eyebrow light"><MapPin size={15} /> {hotel.area}, {hotel.city}</div>
              <h1>{hotel.name}</h1>
              <div className="detail-rating"><Star fill="currentColor" /> {hotel.rating.toFixed(1)} <span>· {hotel.reviewCount} guest signals</span></div>
            </div>
            <div className="detail-badge"><ShieldCheck /><span>StayFinder verified</span></div>
          </div>
        </div>
      </section>

      <div className="container detail-layout">
        <section className="detail-content">
          <div className="detail-block">
            <span className="section-kicker">The property</span>
            <h2>A stay designed around the way you travel</h2>
            <p className="detail-description">{hotel.description}</p>
            <div className="feature-strip">
              <span><Wifi /> Fast connectivity</span>
              <span><BedDouble /> Flexible rooms</span>
              <span><ShieldCheck /> Verified inventory</span>
            </div>
          </div>

          <div className="detail-block">
            <div className="section-heading compact">
              <div><span className="section-kicker">Room inventory</span><h2>Choose your room</h2></div>
              <span className="live-indicator"><i /> Live availability</span>
            </div>
            <div className="room-list">
              {hotel.rooms.map((item) => (
                <button
                  key={item.id}
                  disabled={!item.available}
                  className={`room-option ${selectedRoom === item.id ? 'selected' : ''}`}
                  onClick={() => setSelectedRoom(item.id)}
                >
                  <span className="room-radio">{selectedRoom === item.id && <Check size={15} />}</span>
                  <span className="room-copy"><strong>{item.roomType}</strong><small><Users size={14} /> Up to {item.capacity} guests</small></span>
                  <span className="room-price"><strong>{formatMoney(item.pricePerNight)}</strong><small>/ night</small></span>
                  <span className={`availability ${item.available ? '' : 'unavailable'}`}>{item.available ? 'Available' : 'Booked'}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="detail-block">
            <span className="section-kicker">Amenities</span>
            <h2>Everything included</h2>
            <div className="amenities-large">
              {hotel.amenities.map((amenity) => <span key={amenity}><Check size={16} /> {amenity}</span>)}
            </div>
          </div>

          <div className="detail-block">
            <div className="section-heading compact"><div><span className="section-kicker">Guest voice</span><h2>Recent reviews</h2></div></div>
            <div className="review-grid">
              {hotel.reviews.slice(0, 6).map((review) => (
                <article className="review-card" key={review.id}>
                  <div className="review-top"><div className="avatar">{review.userName[0]}</div><div><strong>{review.userName}</strong><span>{'★'.repeat(review.rating)}</span></div></div>
                  <p>{review.comment}</p>
                </article>
              ))}
            </div>
            <form className="review-form" onSubmit={submitReview}>
              <div><strong>Share your experience</strong><span>One review per traveller; submitting again updates it.</span></div>
              <select value={reviewRating} onChange={(event) => setReviewRating(Number(event.target.value))}>
                {[5, 4, 3, 2, 1].map((value) => <option key={value} value={value}>{value} stars</option>)}
              </select>
              <input required value={reviewComment} onChange={(event) => setReviewComment(event.target.value)} placeholder="What stood out?" />
              <button className="button button-small">Publish</button>
            </form>
          </div>
        </section>

        <aside className="booking-card-wrap">
          <form className="booking-card" onSubmit={createBooking}>
            <div className="booking-card-heading"><span>Reserve your stay</span><strong>{room ? formatMoney(room.pricePerNight) : '—'} <small>/ night</small></strong></div>
            <div className="booking-date-grid">
              <label><span>Check-in</span><input type="date" min={todayIso()} value={checkIn} onChange={(event) => setCheckIn(event.target.value)} /></label>
              <label><span>Check-out</span><input type="date" min={checkIn} value={checkOut} onChange={(event) => setCheckOut(event.target.value)} /></label>
            </div>
            <label className="booking-guests"><span>Guests</span><select value={guests} onChange={(event) => setGuests(Number(event.target.value))}>{[1, 2, 3, 4].map((count) => <option key={count}>{count}</option>)}</select></label>
            <div className="booking-summary">
              <span>{room?.roomType || 'No room available'}</span>
              <span>{nights} nights</span>
              <div><span>Total</span><strong>{room ? formatMoney(room.pricePerNight * nights) : '—'}</strong></div>
            </div>
            {notice && <div className="inline-success">{notice}</div>}
            {error && <div className="inline-error">{error}</div>}
            <button className="button button-full" disabled={!room || booking}>{booking ? 'Securing room…' : user ? 'Confirm booking' : 'Sign in to book'}</button>
            <p className="booking-note"><ShieldCheck size={15} /> A database overlap check runs again at confirmation.</p>
          </form>
        </aside>
      </div>
    </main>
  );
}
