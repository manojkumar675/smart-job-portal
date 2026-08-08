import React, { useEffect, useState, useCallback } from "react";
import api from "../../api/axiosConfig";

export default function AdminDashboard() {
  const [usersPage, setUsersPage] = useState({ content: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [message, setMessage] = useState("");
  const [jobIdToModerate, setJobIdToModerate] = useState("");

  const fetchUsers = useCallback(async () => {
    try {
      const { data } = await api.get("/admin/users", { params: { page, size: 10 } });
      setUsersPage(data);
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to load users.");
    }
  }, [page]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleToggleEnabled = async (user) => {
    try {
      await api.patch(`/admin/users/${user.id}/status`, null, { params: { enabled: !user.enabled } });
      fetchUsers();
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to update user.");
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm("Delete this user permanently?")) return;
    try {
      await api.delete(`/admin/users/${userId}`);
      fetchUsers();
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to delete user.");
    }
  };

  const handleDeleteJob = async (e) => {
    e.preventDefault();
    if (!jobIdToModerate) return;
    try {
      await api.delete(`/admin/jobs/${jobIdToModerate}`);
      setMessage(`Job ${jobIdToModerate} deleted.`);
      setJobIdToModerate("");
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to delete job.");
    }
  };

  return (
    <div className="page-container">
      <h2>Admin Panel</h2>
      {message && <div className="alert-info">{message}</div>}

      <h3>Users</h3>
      <table className="data-table">
        <thead>
          <tr>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Joined</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {usersPage.content?.map((u) => (
            <tr key={u.id}>
              <td>{u.email}</td>
              <td>{u.role}</td>
              <td>
                <span className={u.enabled ? "status-badge status-applied" : "status-badge status-rejected"}>
                  {u.enabled ? "Enabled" : "Disabled"}
                </span>
              </td>
              <td>{new Date(u.createdAt).toLocaleDateString()}</td>
              <td className="actions-cell">
                <button onClick={() => handleToggleEnabled(u)}>
                  {u.enabled ? "Disable" : "Enable"}
                </button>
                <button className="btn-danger" onClick={() => handleDeleteUser(u.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage((p) => Math.max(p - 1, 0))}>
          Previous
        </button>
        <span>
          Page {page + 1} of {Math.max(usersPage.totalPages, 1)}
        </span>
        <button disabled={page + 1 >= usersPage.totalPages} onClick={() => setPage((p) => p + 1)}>
          Next
        </button>
      </div>

      <h3>Moderate a job listing</h3>
      <form onSubmit={handleDeleteJob} className="filter-bar">
        <input
          placeholder="Job ID"
          value={jobIdToModerate}
          onChange={(e) => setJobIdToModerate(e.target.value)}
        />
        <button type="submit" className="btn-danger">
          Delete job by ID
        </button>
      </form>
    </div>
  );
}
