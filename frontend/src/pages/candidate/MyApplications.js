import React, { useEffect, useState } from "react";
import api from "../../api/axiosConfig";

export default function MyApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    api
      .get("/applications/mine")
      .then(({ data }) => setApplications(data))
      .catch((err) => setMessage(err.response?.data?.message || "Failed to load applications."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page-container">
      <h2>My Applications</h2>
      {message && <div className="alert-error">{message}</div>}
      {loading && <p>Loading...</p>}

      <table className="data-table">
        <thead>
          <tr>
            <th>Job</th>
            <th>Status</th>
            <th>Match %</th>
            <th>Applied On</th>
          </tr>
        </thead>
        <tbody>
          {applications.map((app) => (
            <tr key={app.id}>
              <td>{app.jobTitle}</td>
              <td>
                <span className={`status-badge status-${app.status.toLowerCase()}`}>
                  {app.status}
                </span>
              </td>
              <td>{app.matchPercentage}%</td>
              <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {applications.length === 0 && !loading && <p>You haven't applied to any jobs yet.</p>}
    </div>
  );
}
