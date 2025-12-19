import { createContext, useState, useEffect } from "react";
import { API_BASE } from "../config/api";
import { useAuth } from "./AuthContext";

export const LibraryContext = createContext();

export function LibraryProvider({ children }) {
  const [loading, setLoading] = useState(true);
  const [library, setLibrary] = useState(() => {
    const saved = localStorage.getItem("library");
    return saved ? JSON.parse(saved) : [];
  });

  const { user } = useAuth();

  useEffect(() => {
    const fetchLibrary = async () => {
      if (!user) {
        setLibrary([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);

        const response = await fetch(`${API_BASE}/api/library`, {
          headers: {
            Authorization: `Bearer ${user.token}`,
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch library");
        }

        const data = await response.json();

        const formatted = data.map((entry) => ({
          id: entry.manga.id,
          title: entry.manga.title,
          author: entry.manga.author,
          coverUrl: entry.manga.coverUrl,
          rating: entry.manga.rating ?? 0,
          readingStatus: entry.readingStatus ?? "Plan to Read",
          year: entry.manga.year,
        }));

        setLibrary(formatted);
      } catch (err) {
        console.error("Library: Error fetching library:", err);
        setLibrary([]);
      } finally {
        setLoading(false);
      }
    };

    fetchLibrary();
  }, [user]);

  return (
    <LibraryContext.Provider value={{ library, setLibrary, loading }}>
      {children}
    </LibraryContext.Provider>
  );
}
