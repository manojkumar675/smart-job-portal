import React, { useEffect, useState, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../../api/axiosConfig";

export default function RecruiterDashboard() {
  const [jobsPage, setJobsPage] = useState({ content: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const fetchJobs = useCallback(async () => {
    try {
      const { data } = await api.get("/recruiter/jobs", { params: { page, size: 8 } });
      setJobsPage(data);
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to load your jobs.");
    }
  }, [page]);

  useEffect(() => {
    fetchJobs();
  }, [fetchJobs]);

  const handleDelete = async (jobId) => {
    if (!window.confirm("Delete this job listing?")) return;
    try {
      await api.delete(`/jobs/${jobId}`);
      fetchJobs();
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to delete job.");
    }
  };

  const handleToggleActive = async (job) => {
    try {
      await api.patch(`/jobs/${job.id}/status`, null, { params: { active: !job.active } });
      fetchJobs();
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to update job status.");
    }
  };

  return (
    <div className="page-container">
      <div className="dashboard-header">
        <h2>My Job Listings</h2>
        <Link to="/recruiter/post-job" className="btn">
          + Post a new job
        </Link>
      </div>

      {message && <div className="alert-error">{message}</div>}

      <table className="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Location</th>
            <th>Type</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {jobsPage.content?.map((job) => (
            <tr key={job.id}>
              <td>{job.title}</td>
              <td>{job.location}</td>
              <td>{job.jobType?.replace("_", " ")}</td>
              <td>
                <span className={job.active ? "status-badge status-applied" : "status-badge status-rejected"}>
                  {job.active ? "Active" : "Inactive"}
                </span>
              </td>
              <td className="actions-cell">
                <button onClick={() => navigate(`/recruiter/post-job?edit=${job.id}`)}>Edit</button>
                <button onClick={() => navigate(`/recruiter/jobs/${job.id}/applicants`)}>
                  Applicants
                </button>
                <button onClick={() => handleToggleActive(job)}>
                  {job.active ? "Deactivate" : "Activate"}
                </button>
                <button className="btn-danger" onClick={() => handleDelete(job.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {jobsPage.content?.length === 0 && <p>You haven't posted any jobs yet.</p>}

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage((p) => Math.max(p - 1, 0))}>
          Previous
        </button>
        <span>
          Page {page + 1} of {Math.max(jobsPage.totalPages, 1)}
        </span>
        <button disabled={page + 1 >= jobsPage.totalPages} onClick={() => setPage((p) => p + 1)}>
          Next
        </button>
      </div>
    </div>
  );
}
