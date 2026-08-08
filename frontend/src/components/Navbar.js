import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        Smart Job Portal
      </Link>
      <div className="navbar-links">
        {!user && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
        {user && user.role === "CANDIDATE" && (
          <>
            <Link to="/candidate/jobs">Find Jobs</Link>
            <Link to="/candidate/applications">My Applications</Link>
            <Link to="/candidate/profile">Profile</Link>
          </>
        )}
        {user && user.role === "RECRUITER" && (
          <>
            <Link to="/recruiter/dashboard">Dashboard</Link>
            <Link to="/recruiter/post-job">Post Job</Link>
          </>
        )}
        {user && user.role === "ADMIN" && <Link to="/admin/dashboard">Admin Panel</Link>}
        {user && (
          <>
            <span className="navbar-user">
              {user.email} ({user.role})
            </span>
            <button onClick={handleLogout} className="btn-link">
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
