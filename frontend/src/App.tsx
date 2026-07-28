import { Route, Routes, useLocation } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { DashboardPage } from './pages/DashboardPage';
import { HomePage } from './pages/HomePage';
import { HotelPage } from './pages/HotelPage';
import { LoginPage } from './pages/LoginPage';
import { MyBookingsPage } from './pages/MyBookingsPage';

export default function App() {
  const location = useLocation();
  const hideNavbar = location.pathname === '/login';

  return (
    <>
      {!hideNavbar && <Navbar />}
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/hotels/:id" element={<HotelPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/bookings" element={<MyBookingsPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="*" element={<HomePage />} />
      </Routes>
      {!hideNavbar && (
        <footer className="footer">
          <div className="container"><span>StayFinder</span><p>Built with Java, Spring Boot, React, SQL, DSA and Docker.</p><small>Portfolio project · Demo data only</small></div>
        </footer>
      )}
    </>
  );
}
