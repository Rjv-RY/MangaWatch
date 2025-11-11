import { createContext, useContext, useState, useEffect } from "react";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Load user info from localStorage on page load
  useEffect(() => {
    const token = localStorage.getItem("manga_token");
    const username = localStorage.getItem("manga_username");
    if (token && username) {
      setUser({ username, token });
      setLoading(false);
    }
  }, []);

  const login = (data) => {
    localStorage.setItem("manga_token", data.token);
    localStorage.setItem("manga_username", data.username);
    setUser({ username: data.username, token: data.token }); //key here
  };

  const logout = () => {
    localStorage.removeItem("manga_token");
    localStorage.removeItem("manga_username");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
