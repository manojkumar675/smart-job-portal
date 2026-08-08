import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const roleHome = {
  CANDIDATE: "/candidate/jobs",
  RECRUITER: "/recruiter/dashboard",
};

export default function Register() {
  const { register, loading, error } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    role: "CANDIDATE",
    fullName: "",
    companyName: "",
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = await register(form);
      navigate(roleHome[data.role] || "/");
    } catch {
      // error surfaced via context
    }
  };

  return (
    <div className="page-container narrow">
      <h2>Create an account</h2>
      {error && <div className="alert-error">{error}</div>}
      <form onSubmit={handleSubmit} className="form">
        <label>I am a</label>
        <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
          <option value="CANDIDATE">Candidate</option>
          <option value="RECRUITER">Recruiter</option>
        </select>

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
          minLength={6}
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />

        {form.role === "CANDIDATE" && (
          <>
            <label>Full name</label>
            <input
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            />
          </>
        )}

        {form.role === "RECRUITER" && (
          <>
            <label>Company name</label>
            <input
              value={form.companyName}
              onChange={(e) => setForm({ ...form, companyName: e.target.value })}
            />
          </>
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Creating account..." : "Register"}
        </button>
      </form>
    </div>
  );
}
