import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Home() {
  const { user } = useAuth();

  return (
    <div className="page-container">
      <h1>Smart Job Portal</h1>
      <p>Find your next role, or find your next hire.</p>
      {!user && (
        <div className="cta-row">
          <Link to="/register" className="btn">
            Get started
          </Link>
          <Link to="/login" className="btn-secondary">
            Login
          </Link>
        </div>
      )}
    </div>
  );
}
