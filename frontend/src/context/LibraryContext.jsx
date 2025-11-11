import { createContext, useState, useEffect } from "react";

export const LibraryContext = createContext();

export function LibraryProvider({ children }) {
  const [library, setLibrary] = useState(() => {
    const saved = localStorage.getItem("library");
    return saved ? JSON.parse(saved) : [];
  });

  useEffect(() => {
    const fetchLibrary = async () => {
      const token = localStorage.getItem("manga_token");
      if (!token) {
        console.error("No token found in localStorage");
        return;
      }

      try {
        const response = await fetch("http://localhost:8080/api/library", {
          headers: {
            Authorization: `Bearer ${token}`,
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
        console.error("Error fetching library:", err);
      }
    };

    fetchLibrary();
  }, [setLibrary]);

  return (
    <LibraryContext.Provider value={{ library, setLibrary }}>
      {children}
    </LibraryContext.Provider>
  );
}
