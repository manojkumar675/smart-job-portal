import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../../api/axiosConfig";

const JOB_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "REMOTE"];
const EXPERIENCE_LEVELS = ["ENTRY", "MID", "SENIOR", "LEAD"];

const emptyForm = {
  title: "",
  description: "",
  location: "",
  jobType: "FULL_TIME",
  experienceLevel: "ENTRY",
  minSalary: "",
  maxSalary: "",
  requiredSkills: "",
};

export default function PostJob() {
  const [searchParams] = useSearchParams();
  const editId = searchParams.get("edit");
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!editId) return;
    api.get(`/jobs/${editId}`).then(({ data }) => {
      setForm({
        title: data.title,
        description: data.description,
        location: data.location,
        jobType: data.jobType,
        experienceLevel: data.experienceLevel,
        minSalary: data.minSalary ?? "",
        maxSalary: data.maxSalary ?? "",
        requiredSkills: (data.requiredSkills || []).join(", "),
      });
    });
  }, [editId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      const payload = {
        title: form.title,
        description: form.description,
        location: form.location,
        jobType: form.jobType,
        experienceLevel: form.experienceLevel,
        minSalary: form.minSalary ? Number(form.minSalary) : null,
        maxSalary: form.maxSalary ? Number(form.maxSalary) : null,
        requiredSkills: form.requiredSkills.split(",").map((s) => s.trim()).filter(Boolean),
      };

      if (editId) {
        await api.put(`/jobs/${editId}`, payload);
      } else {
        await api.post("/jobs", payload);
      }
      navigate("/recruiter/dashboard");
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to save job listing.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-container narrow">
      <h2>{editId ? "Edit Job" : "Post a New Job"}</h2>
      {message && <div className="alert-error">{message}</div>}
      <form onSubmit={handleSubmit} className="form">
        <label>Title</label>
        <input
          required
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
        />

        <label>Description</label>
        <textarea
          required
          rows={5}
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />

        <label>Location</label>
        <input
          required
          value={form.location}
          onChange={(e) => setForm({ ...form, location: e.target.value })}
        />

        <label>Job type</label>
        <select value={form.jobType} onChange={(e) => setForm({ ...form, jobType: e.target.value })}>
          {JOB_TYPES.map((t) => (
            <option key={t} value={t}>
              {t.replace("_", " ")}
            </option>
          ))}
        </select>

        <label>Experience level</label>
        <select
          value={form.experienceLevel}
          onChange={(e) => setForm({ ...form, experienceLevel: e.target.value })}
        >
          {EXPERIENCE_LEVELS.map((l) => (
            <option key={l} value={l}>
              {l}
            </option>
          ))}
        </select>

        <label>Min salary</label>
        <input
          type="number"
          value={form.minSalary}
          onChange={(e) => setForm({ ...form, minSalary: e.target.value })}
        />

        <label>Max salary</label>
        <input
          type="number"
          value={form.maxSalary}
          onChange={(e) => setForm({ ...form, maxSalary: e.target.value })}
        />

        <label>Required skills (comma separated)</label>
        <input
          required
          value={form.requiredSkills}
          onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })}
        />

        <button type="submit" disabled={saving}>
          {saving ? "Saving..." : editId ? "Update job" : "Post job"}
        </button>
      </form>
    </div>
  );
}
