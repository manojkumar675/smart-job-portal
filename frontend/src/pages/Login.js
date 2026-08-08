import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const roleHome = {
  CANDIDATE: "/candidate/jobs",
  RECRUITER: "/recruiter/dashboard",
  ADMIN: "/admin/dashboard",
};

export default function Login() {
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = await login(form.email, form.password);
      navigate(roleHome[data.role] || "/");
    } catch {
      // error is surfaced via context state
    }
  };

  return (
    <div className="page-container narrow">
      <h2>Login</h2>
      {error && <div className="alert-error">{error}</div>}
      <form onSubmit={handleSubmit} className="form">
        <label>Email</label>
        <input
          type="email"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />
        <label>Password</label>
        <input
          type="password"
          required
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />
        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
      <p className="hint">
        Demo accounts (password: password123): admin@jobportal.com, recruiter1@acme.com,
        candidate1@example.com
      </p>
    </div>
  );
}
