import { CalendarDays, MapPin, PlaneTakeoff, XCircle } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { ErrorView, LoadingView } from '../components/StatusView';
import { useAuth } from '../context/AuthContext';
import type { BookingData } from '../types';
import { formatDate, formatMoney } from '../utils/format';

export function MyBookingsPage() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState<BookingData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => {
    if (!user) return;
    setLoading(true);
    api<BookingData[]>('/bookings/me', {}, user.token)
      .then(setBookings)
      .catch((reason: ApiError) => setError(reason.message))
      .finally(() => setLoading(false));
  };

  useEffect(load, [user]);

  if (!user) return <Navigate to="/login" state={{ from: '/bookings' }} replace />;

  const cancel = async (id: number) => {
    if (!window.confirm('Cancel this booking?')) return;
    try {
      await api(`/bookings/${id}/cancel`, { method: 'PATCH' }, user.token);
      load();
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Could not cancel booking');
    }
  };

  return (
    <main className="page-shell container">
      <div className="page-title-row">
        <div><span className="section-kicker">Traveller workspace</span><h1>My trips</h1><p>Every confirmed, completed and cancelled reservation in one place.</p></div>
        <div className="page-title-icon"><PlaneTakeoff /></div>
      </div>
      {loading && <LoadingView label="Loading your trips…" />}
      {!loading && error && <ErrorView message={error} />}
      {!loading && !error && bookings.length === 0 && (
        <div className="status-view empty"><PlaneTakeoff /><h3>No trips yet</h3><p>Your next memorable stay starts with a search.</p><Link className="button" to="/">Explore stays</Link></div>
      )}
      {!loading && bookings.length > 0 && (
        <div className="booking-list">
          {bookings.map((booking) => (
            <article className={`trip-card ${booking.status.toLowerCase()}`} key={booking.id}>
              <img src={booking.imageUrl} alt={booking.hotelName} />
              <div className="trip-card-body">
                <div className="trip-top-row"><span className={`status-chip ${booking.status.toLowerCase()}`}>{booking.status}</span><span>Booking #{booking.id}</span></div>
                <h2>{booking.hotelName}</h2>
                <p><MapPin size={15} /> {booking.city} · {booking.roomType}</p>
                <div className="trip-facts">
                  <span><CalendarDays /> <strong>{formatDate(booking.checkIn)}</strong> to <strong>{formatDate(booking.checkOut)}</strong></span>
                  <span>{booking.nights} nights · {booking.guests} guests</span>
                </div>
              </div>
              <div className="trip-actions">
                <strong>{formatMoney(booking.totalAmount)}</strong>
                <Link to={`/hotels/${booking.hotelId}`}>View property</Link>
                {booking.status === 'CONFIRMED' && new Date(`${booking.checkIn}T00:00:00`) > new Date() && (
                  <button onClick={() => cancel(booking.id)}><XCircle size={16} /> Cancel</button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
