import React, { useEffect, useState, useCallback } from "react";
import api from "../../api/axiosConfig";

const JOB_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "REMOTE"];
const EXPERIENCE_LEVELS = ["ENTRY", "MID", "SENIOR", "LEAD"];

export default function JobSearch() {
  const [filters, setFilters] = useState({
    keyword: "",
    location: "",
    skills: "",
    jobType: "",
    experienceLevel: "",
  });
  const [page, setPage] = useState(0);
  const [jobsPage, setJobsPage] = useState({ content: [], totalPages: 0 });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [appliedJobIds, setAppliedJobIds] = useState(new Set());

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 6, sort: "createdAt,desc" };
      if (filters.keyword) params.keyword = filters.keyword;
      if (filters.location) params.location = filters.location;
      if (filters.jobType) params.jobType = filters.jobType;
      if (filters.experienceLevel) params.experienceLevel = filters.experienceLevel;
      if (filters.skills) {
        params.skills = filters.skills.split(",").map((s) => s.trim()).filter(Boolean);
      }
      const { data } = await api.get("/jobs", { params });
      setJobsPage(data);
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to load jobs.");
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    fetchJobs();
    api.get("/applications/mine")
      .then(({ data }) => {
        const ids = data.map(app => app.jobId);
        setAppliedJobIds(new Set(ids));
      })
      .catch(console.error);
  }, [fetchJobs]);

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchJobs();
  };

  const handleApply = async (jobId) => {
    setMessage("");
    try {
      await api.post(`/applications/apply/${jobId}`);
      setMessage("Applied successfully!");
      setAppliedJobIds((prev) => new Set(prev).add(jobId));
    } catch (err) {
      setMessage(err.response?.data?.message || "Could not apply to this job.");
    }
  };

  return (
    <div className="page-container">
      <h2>Find Jobs</h2>

      <form onSubmit={handleFilterSubmit} className="filter-bar">
        <input
          placeholder="Keyword (title/description)"
          value={filters.keyword}
          onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
        />
        <input
          placeholder="Location"
          value={filters.location}
          onChange={(e) => setFilters({ ...filters, location: e.target.value })}
        />
        <input
          placeholder="Skills (comma separated)"
          value={filters.skills}
          onChange={(e) => setFilters({ ...filters, skills: e.target.value })}
        />
        <select
          value={filters.jobType}
          onChange={(e) => setFilters({ ...filters, jobType: e.target.value })}
        >
          <option value="">Any job type</option>
          {JOB_TYPES.map((t) => (
            <option key={t} value={t}>
              {t.replace("_", " ")}
            </option>
          ))}
        </select>
        <select
          value={filters.experienceLevel}
          onChange={(e) => setFilters({ ...filters, experienceLevel: e.target.value })}
        >
          <option value="">Any experience level</option>
          {EXPERIENCE_LEVELS.map((l) => (
            <option key={l} value={l}>
              {l}
            </option>
          ))}
        </select>
        <button type="submit">Search</button>
      </form>

      {message && <div className="alert-info">{message}</div>}
      {loading && <p>Loading jobs...</p>}

      <div className="job-grid">
        {jobsPage.content?.map((job) => (
          <div className="job-card" key={job.id}>
            <h3>{job.title}</h3>
            <p className="job-company">{job.companyName}</p>
            <p>
              {job.location} &middot; {job.jobType?.replace("_", " ")} &middot; {job.experienceLevel}
            </p>
            <p className="job-description">{job.description}</p>
            <div className="skill-tags">
              {job.requiredSkills?.map((s) => (
                <span key={s} className="skill-tag">
                  {s}
                </span>
              ))}
            </div>
            {job.matchPercentage !== null && job.matchPercentage !== undefined && (
              <div className="match-bar-container">
                <div className="match-bar" style={{ width: `${job.matchPercentage}%` }} />
                <span className="match-label">{job.matchPercentage}% match</span>
              </div>
            )}
            <button 
              disabled={appliedJobIds.has(job.id)} 
              onClick={() => handleApply(job.id)}
            >
              {appliedJobIds.has(job.id) ? "Applied" : "Apply"}
            </button>
          </div>
        ))}
      </div>

      {jobsPage.content?.length === 0 && !loading && <p>No jobs match your filters.</p>}

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage((p) => Math.max(p - 1, 0))}>
          Previous
        </button>
        <span>
          Page {page + 1} of {Math.max(jobsPage.totalPages, 1)}
        </span>
        <button
          disabled={page + 1 >= jobsPage.totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
