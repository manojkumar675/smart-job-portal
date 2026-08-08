import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import PrivateRoute from "./components/PrivateRoute";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";

import JobSearch from "./pages/candidate/JobSearch";
import MyApplications from "./pages/candidate/MyApplications";
import CandidateProfile from "./pages/candidate/CandidateProfile";

import RecruiterDashboard from "./pages/recruiter/RecruiterDashboard";
import PostJob from "./pages/recruiter/PostJob";
import JobApplicants from "./pages/recruiter/JobApplicants";

import AdminDashboard from "./pages/admin/AdminDashboard";

import "./styles.css";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Candidate routes */}
          <Route
            path="/candidate/jobs"
            element={
              <PrivateRoute allowedRoles={["CANDIDATE"]}>
                <JobSearch />
              </PrivateRoute>
            }
          />
          <Route
            path="/candidate/applications"
            element={
              <PrivateRoute allowedRoles={["CANDIDATE"]}>
                <MyApplications />
              </PrivateRoute>
            }
          />
          <Route
            path="/candidate/profile"
            element={
              <PrivateRoute allowedRoles={["CANDIDATE"]}>
                <CandidateProfile />
              </PrivateRoute>
            }
          />

          {/* Recruiter routes */}
          <Route
            path="/recruiter/dashboard"
            element={
              <PrivateRoute allowedRoles={["RECRUITER"]}>
                <RecruiterDashboard />
              </PrivateRoute>
            }
          />
          <Route
            path="/recruiter/post-job"
            element={
              <PrivateRoute allowedRoles={["RECRUITER"]}>
                <PostJob />
              </PrivateRoute>
            }
          />
          <Route
            path="/recruiter/jobs/:jobId/applicants"
            element={
              <PrivateRoute allowedRoles={["RECRUITER"]}>
                <JobApplicants />
              </PrivateRoute>
            }
          />

          {/* Admin routes */}
          <Route
            path="/admin/dashboard"
            element={
              <PrivateRoute allowedRoles={["ADMIN"]}>
                <AdminDashboard />
              </PrivateRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
