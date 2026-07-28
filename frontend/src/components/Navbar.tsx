import { BarChart3, BedDouble, LogOut, Menu, Sparkles, X } from 'lucide-react';
import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);

  return (
    <header className="navbar-shell">
      <nav className="navbar container">
        <Link to="/" className="brand" aria-label="StayFinder home">
          <span className="brand-mark"><Sparkles size={18} /></span>
          <span>Stay<span>Finder</span></span>
        </Link>

        <button className="icon-button mobile-menu-button" onClick={() => setOpen(!open)} aria-label="Toggle menu">
          {open ? <X /> : <Menu />}
        </button>

        <div className={`nav-links ${open ? 'open' : ''}`}>
          <NavLink to="/" onClick={() => setOpen(false)}>Discover</NavLink>
          {user && (
            <NavLink to="/bookings" onClick={() => setOpen(false)}>
              <BedDouble size={17} /> My trips
            </NavLink>
          )}
          {user?.role === 'ADMIN' && (
            <NavLink to="/dashboard" onClick={() => setOpen(false)}>
              <BarChart3 size={17} /> Analytics
            </NavLink>
          )}
          {user ? (
            <div className="account-menu">
              <div className="avatar">{user.name.slice(0, 1).toUpperCase()}</div>
              <div className="account-copy">
                <strong>{user.name}</strong>
                <span>{user.role === 'ADMIN' ? 'Administrator' : 'Traveller'}</span>
              </div>
              <button className="icon-button" onClick={logout} title="Log out"><LogOut size={18} /></button>
            </div>
          ) : (
            <Link className="button button-small" to="/login" onClick={() => setOpen(false)}>Sign in</Link>
          )}
        </div>
      </nav>
    </header>
  );
}
