import React, { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import api from "../../api/axiosConfig";

const STATUSES = ["APPLIED", "SHORTLISTED", "REJECTED", "HIRED"];

export default function JobApplicants() {
  const { jobId } = useParams();
  const [applicantsPage, setApplicantsPage] = useState({ content: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [message, setMessage] = useState("");

  const fetchApplicants = useCallback(async () => {
    try {
      const { data } = await api.get(`/applications/job/${jobId}`, { params: { page, size: 10 } });
      setApplicantsPage(data);
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to load applicants.");
    }
  }, [jobId, page]);

  useEffect(() => {
    fetchApplicants();
  }, [fetchApplicants]);

  const handleStatusChange = async (applicationId, status) => {
    try {
      await api.patch(`/applications/${applicationId}/status`, { status });
      fetchApplicants();
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to update status.");
    }
  };

  return (
    <div className="page-container">
      <h2>Applicants</h2>
      {message && <div className="alert-error">{message}</div>}

      <table className="data-table">
        <thead>
          <tr>
            <th>Candidate</th>
            <th>Email</th>
            <th>Match %</th>
            <th>Status</th>
            <th>Applied On</th>
          </tr>
        </thead>
        <tbody>
          {applicantsPage.content?.map((app) => (
            <tr key={app.id}>
              <td>{app.candidateName}</td>
              <td>{app.candidateEmail}</td>
              <td>{app.matchPercentage}%</td>
              <td>
                <select
                  value={app.status}
                  onChange={(e) => handleStatusChange(app.id, e.target.value)}
                >
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </td>
              <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {applicantsPage.content?.length === 0 && <p>No applicants yet for this job.</p>}

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage((p) => Math.max(p - 1, 0))}>
          Previous
        </button>
        <span>
          Page {page + 1} of {Math.max(applicantsPage.totalPages, 1)}
        </span>
        <button
          disabled={page + 1 >= applicantsPage.totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
