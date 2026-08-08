import React, { createContext, useContext, useState, useCallback } from "react";
import api, { setAuthToken } from "../api/axiosConfig";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // user + token both live in React state only -> lost on page refresh, never persisted to storage
  const [user, setUser] = useState(null); // { email, role, userId }
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const login = useCallback(async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.post("/auth/login", { email, password });
      setAuthToken(data.token);
      setUser({ email: data.email, role: data.role, userId: data.userId });
      return data;
    } catch (err) {
      const msg = err.response?.data?.message || "Login failed. Check your credentials.";
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (payload) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.post("/auth/register", payload);
      setAuthToken(data.token);
      setUser({ email: data.email, role: data.role, userId: data.userId });
      return data;
    } catch (err) {
      const msg = err.response?.data?.message || "Registration failed.";
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    setAuthToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading, error }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
