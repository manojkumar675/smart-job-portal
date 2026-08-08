import axios from "axios";

// Token lives only in memory (a plain JS variable), never in localStorage/sessionStorage.
// AuthContext calls setAuthToken() whenever the user logs in/out.
let inMemoryToken = null;

export function setAuthToken(token) {
  inMemoryToken = token;
}

export function getAuthToken() {
  return inMemoryToken;
}

const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  if (inMemoryToken) {
    config.headers.Authorization = `Bearer ${inMemoryToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Centralized handling so pages don't each have to check for 401/403
    if (error.response && error.response.status === 401) {
      setAuthToken(null);
    }
    return Promise.reject(error);
  }
);

export default api;
