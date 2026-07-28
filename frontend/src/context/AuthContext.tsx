import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import type { AuthUser } from '../types';
import { getStoredUser } from '../api/client';

interface AuthContextValue {
  user: AuthUser | null;
  login: (user: AuthUser) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => getStoredUser());

  const value = useMemo<AuthContextValue>(() => ({
    user,
    login: (nextUser) => {
      localStorage.setItem('stayfinder_user', JSON.stringify(nextUser));
      setUser(nextUser);
    },
    logout: () => {
      localStorage.removeItem('stayfinder_user');
      setUser(null);
    },
  }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used inside AuthProvider');
  return value;
}
