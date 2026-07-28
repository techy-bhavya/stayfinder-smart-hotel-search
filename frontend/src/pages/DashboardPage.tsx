import { BadgeIndianRupee, BarChart3, Building2, CalendarCheck2, MapPinned, Percent, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { api, ApiError } from '../api/client';
import { KpiCard } from '../components/KpiCard';
import { ErrorView, LoadingView } from '../components/StatusView';
import { useAuth } from '../context/AuthContext';
import type { AnalyticsData } from '../types';
import { formatMoney } from '../utils/format';

export function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') return;
    api<AnalyticsData>('/analytics/overview', {}, user.token)
      .then(setData)
      .catch((reason: ApiError) => setError(reason.message))
      .finally(() => setLoading(false));
  }, [user]);

  if (!user) return <Navigate to="/login" state={{ from: '/dashboard' }} replace />;
  if (user.role !== 'ADMIN') return <Navigate to="/" replace />;

  return (
    <main className="dashboard-page">
      <div className="container">
        <div className="dashboard-header">
          <div>
            <div className="eyebrow"><BarChart3 size={16} /> Decision intelligence</div>
            <h1>Hospitality command centre</h1>
            <p>Revenue, demand, occupancy and property performance—all derived from booking data.</p>
          </div>
          <div className="live-data-pill"><i /> Live data model</div>
        </div>

        {loading && <LoadingView label="Calculating business KPIs…" />}
        {!loading && error && <ErrorView message={error} />}
        {!loading && data && (
          <>
            <section className="kpi-grid">
              <KpiCard icon={BadgeIndianRupee} label="Total revenue" value={formatMoney(data.kpis.totalRevenue)} helper="Confirmed booking value" />
              <KpiCard icon={CalendarCheck2} label="Confirmed bookings" value={String(data.kpis.confirmedBookings)} helper="Across all properties" />
              <KpiCard icon={Percent} label="30-day occupancy" value={`${data.kpis.occupancyRate.toFixed(1)}%`} helper="Room-night utilisation" />
              <KpiCard icon={TrendingUp} label="Cancellation rate" value={`${data.kpis.cancellationRate.toFixed(1)}%`} helper="Share of all bookings" />
              <KpiCard icon={MapPinned} label="Top destination" value={data.kpis.topCity} helper="By confirmed bookings" />
            </section>

            <section className="dashboard-grid">
              <article className="chart-card wide">
                <div className="chart-heading"><div><span>Revenue trajectory</span><h2>Six-month booking value</h2></div><TrendingUp /></div>
                <div className="chart-wrap">
                  <ResponsiveContainer width="100%" height={310}>
                    <AreaChart data={data.monthlyRevenue} margin={{ top: 12, right: 16, bottom: 0, left: 0 }}>
                      <defs>
                        <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#54d7c4" stopOpacity={0.45} />
                          <stop offset="95%" stopColor="#54d7c4" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="4 4" vertical={false} stroke="#243246" />
                      <XAxis dataKey="month" tickLine={false} axisLine={false} tick={{ fill: '#91a0b5', fontSize: 12 }} />
                      <YAxis tickFormatter={(value) => `₹${Math.round(value / 1000)}k`} tickLine={false} axisLine={false} tick={{ fill: '#91a0b5', fontSize: 12 }} />
                      <Tooltip contentStyle={{ background: '#101c2d', border: '1px solid #2a3a50', borderRadius: 14 }} formatter={(value) => formatMoney(Number(value))} />
                      <Area type="monotone" dataKey="revenue" stroke="#54d7c4" strokeWidth={3} fill="url(#revenueFill)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </article>

              <article className="chart-card">
                <div className="chart-heading"><div><span>Market split</span><h2>Revenue by city</h2></div><MapPinned /></div>
                <div className="chart-wrap">
                  <ResponsiveContainer width="100%" height={310}>
                    <BarChart data={data.cityPerformance.slice(0, 6)} layout="vertical" margin={{ top: 10, right: 12, bottom: 0, left: 18 }}>
                      <CartesianGrid strokeDasharray="4 4" horizontal={false} stroke="#243246" />
                      <XAxis type="number" hide />
                      <YAxis type="category" dataKey="city" width={72} tickLine={false} axisLine={false} tick={{ fill: '#91a0b5', fontSize: 12 }} />
                      <Tooltip contentStyle={{ background: '#101c2d', border: '1px solid #2a3a50', borderRadius: 14 }} formatter={(value) => formatMoney(Number(value))} />
                      <Bar dataKey="revenue" fill="#7c8cff" radius={[0, 8, 8, 0]} barSize={18} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </article>
            </section>

            <section className="dashboard-grid lower-grid">
              <article className="data-card">
                <div className="chart-heading"><div><span>Property leaderboard</span><h2>Top performers</h2></div><Building2 /></div>
                <div className="leaderboard">
                  {data.topProperties.map((property, index) => (
                    <div key={property.hotelId}>
                      <span className="rank-number">{String(index + 1).padStart(2, '0')}</span>
                      <div><strong>{property.hotelName}</strong><span>{property.city} · {property.bookings} bookings</span></div>
                      <strong>{formatMoney(property.revenue)}</strong>
                    </div>
                  ))}
                </div>
              </article>

              <article className="data-card">
                <div className="chart-heading"><div><span>Business detail</span><h2>City performance</h2></div><BadgeIndianRupee /></div>
                <div className="data-table-wrap">
                  <table>
                    <thead><tr><th>City</th><th>Bookings</th><th>Avg. value</th><th>Revenue</th></tr></thead>
                    <tbody>
                      {data.cityPerformance.map((city) => (
                        <tr key={city.city}><td>{city.city}</td><td>{city.bookings}</td><td>{formatMoney(city.averageBookingValue)}</td><td>{formatMoney(city.revenue)}</td></tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </article>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
