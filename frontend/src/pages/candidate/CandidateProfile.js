import React, { useEffect, useState } from "react";
import api from "../../api/axiosConfig";

const EXPERIENCE_LEVELS = ["ENTRY", "MID", "SENIOR", "LEAD"];

export default function CandidateProfile() {
  const [profile, setProfile] = useState(null);
  const [skillsInput, setSkillsInput] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get("/candidate/profile").then(({ data }) => {
      setProfile(data);
      setSkillsInput((data.skills || []).join(", "));
    });
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      const payload = {
        fullName: profile.fullName,
        phone: profile.phone,
        experienceLevel: profile.experienceLevel,
        resumeUrl: profile.resumeUrl,
        bio: profile.bio,
        skills: skillsInput.split(",").map((s) => s.trim()).filter(Boolean),
      };
      const { data } = await api.put("/candidate/profile", payload);
      setProfile(data);
      setMessage("Profile updated!");
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to save profile.");
    } finally {
      setSaving(false);
    }
  };

  if (!profile) return <div className="page-container">Loading profile...</div>;

  return (
    <div className="page-container narrow">
      <h2>My Profile</h2>
      {message && <div className="alert-info">{message}</div>}
      <form onSubmit={handleSave} className="form">
        <label>Email</label>
        <input value={profile.email} disabled />

        <label>Full name</label>
        <input
          value={profile.fullName || ""}
          onChange={(e) => setProfile({ ...profile, fullName: e.target.value })}
        />

        <label>Phone</label>
        <input
          value={profile.phone || ""}
          onChange={(e) => setProfile({ ...profile, phone: e.target.value })}
        />

        <label>Experience level</label>
        <select
          value={profile.experienceLevel || ""}
          onChange={(e) => setProfile({ ...profile, experienceLevel: e.target.value })}
        >
          <option value="">Select...</option>
          {EXPERIENCE_LEVELS.map((l) => (
            <option key={l} value={l}>
              {l}
            </option>
          ))}
        </select>

        <label>Resume URL</label>
        <input
          value={profile.resumeUrl || ""}
          onChange={(e) => setProfile({ ...profile, resumeUrl: e.target.value })}
        />

        <label>Bio</label>
        <textarea
          rows={4}
          value={profile.bio || ""}
          onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
        />

        <label>Skills (comma separated)</label>
        <input value={skillsInput} onChange={(e) => setSkillsInput(e.target.value)} />

        <button type="submit" disabled={saving}>
          {saving ? "Saving..." : "Save profile"}
        </button>
      </form>
    </div>
  );
}
