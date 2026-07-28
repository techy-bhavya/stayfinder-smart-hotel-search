import { ArrowLeft, CheckCircle2, KeyRound, Sparkles } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { AuthUser } from '../types';

export function LoginPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('demo@stayfinder.dev');
  const [password, setPassword] = useState('Demo@123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await api<AuthUser>(`/auth/${mode}`, {
        method: 'POST',
        body: JSON.stringify(mode === 'login' ? { email, password } : { name, email, password }),
      });
      login(response);
      const from = (location.state as { from?: string } | null)?.from || (response.role === 'ADMIN' ? '/dashboard' : '/');
      navigate(from, { replace: true });
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Unable to sign in');
    } finally {
      setLoading(false);
    }
  };

  const useAdmin = () => {
    setMode('login');
    setEmail('admin@stayfinder.dev');
    setPassword('Admin@123');
  };

  return (
    <main className="auth-page">
      <div className="auth-visual">
        <Link className="back-link" to="/"><ArrowLeft size={17} /> Back to discovery</Link>
        <div className="auth-visual-content">
          <div className="brand auth-brand"><span className="brand-mark"><Sparkles size={18} /></span><span>Stay<span>Finder</span></span></div>
          <h1>One account.<br />Every better stay.</h1>
          <p>Book rooms, manage trips, publish reviews and explore the admin analytics experience.</p>
          <div className="auth-benefits">
            <span><CheckCircle2 /> Secure JWT authentication</span>
            <span><CheckCircle2 /> BCrypt password protection</span>
            <span><CheckCircle2 /> Role-based access control</span>
          </div>
        </div>
      </div>

      <div className="auth-form-panel">
        <div className="auth-form-card">
          <div className="auth-tabs">
            <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Sign in</button>
            <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Create account</button>
          </div>
          <div className="auth-heading">
            <span className="auth-icon"><KeyRound /></span>
            <div><h2>{mode === 'login' ? 'Welcome back' : 'Start exploring'}</h2><p>{mode === 'login' ? 'Enter your details to continue.' : 'Create your traveller profile.'}</p></div>
          </div>
          <form onSubmit={submit} className="auth-form">
            {mode === 'register' && (
              <label><span>Full name</span><input required minLength={2} value={name} onChange={(event) => setName(event.target.value)} placeholder="Your name" /></label>
            )}
            <label><span>Email address</span><input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /></label>
            <label><span>Password</span><input required type="password" minLength={8} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Minimum 8 characters" /></label>
            {error && <div className="inline-error">{error}</div>}
            <button className="button button-full" disabled={loading}>{loading ? 'Please wait…' : mode === 'login' ? 'Sign in securely' : 'Create my account'}</button>
          </form>
          <div className="demo-box">
            <div><strong>Recruiter demo</strong><span>Open the role-protected analytics dashboard.</span></div>
            <button type="button" onClick={useAdmin}>Use admin</button>
          </div>
        </div>
      </div>
    </main>
  );
}
