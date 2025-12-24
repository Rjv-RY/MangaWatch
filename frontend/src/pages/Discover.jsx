import { useState, useContext, useEffect } from "react";
import { API_BASE } from "../config/api";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { Star, ChevronLeft, ChevronRight, Plus, X } from "lucide-react";
import { LibraryContext } from "../context/LibraryContext.jsx";
import FilterBar from "../components/FilterBar.jsx";
import MangaPage from "../components/MangaPage";
import { useDiscoverParams } from "../hooks/useDiscoverParams.jsx";
import { useAuth } from "../context/AuthContext";

const itemsPerPage = 35;

export default function Discover() {
  console.log("Discover component rendered");
  // get ALL params from URL - single source of truth
  const { query, genres, status, sort, page, setParam } = useDiscoverParams();
  const [mangaData, setMangaData] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams();
  const { library, setLibrary } = useContext(LibraryContext);

  const isInLibrary = (mangaId) => library.some((item) => item.id === mangaId);

  const handleMangaClick = (item) => {
    navigate(`/discover/${item.id}${location.search}`, {
      state: { background: location }, // 👈 keeps /discover mounted underneath
    });
  };

  useEffect(() => {
    const controller = new AbortController();
    const params = new URLSearchParams();

    if (query) params.set("query", query);
    if (genres.length > 0) params.set("genres", genres.join(","));
    if (status.length > 0) params.set("status", status.join(","));
    if (sort) params.set("sort", `${sort},asc`);
    params.set("page", String(page - 1));
    params.set("size", String(itemsPerPage));

    const url = `${API_BASE}/api/manga?${params.toString()}`;

    //logging request temporarily to understand it
    console.groupCollapsed(
      "%c📡 Fetching Manga",
      "color:#0af;font-weight:bold"
    );
    console.log("URL:", url);
    console.log("Params:", {
      query,
      genres,
      status,
      sort,
      page,
      size: itemsPerPage,
    });
    console.groupEnd();

    const headers = {};
    const token = localStorage.getItem("manga_token");
    if (token) headers["Authorization"] = `Bearer ${token}`;

    (async () => {
      try {
        const res = await fetch(url, { signal: controller.signal, headers });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        setMangaData(data.content || []);
        setTotalPages(data.totalPages || 1);
      } catch (err) {
        if (err.name !== "AbortError") {
          console.error("Failed to fetch manga:", err);
          setMangaData([]);
          setTotalPages(1);
        }
      }
    })();

    return () => controller.abort();
  }, [query, genres, status, sort, page]);

  const handlePageChange = (newPage) => {
    setParam("page", newPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // Pagination logic - uses 'page' from URL, not local state
  const getPageNumbers = () => {
    let startPage, endPage;

    if (totalPages <= 5) {
      startPage = 1;
      endPage = totalPages;
    } else if (page <= 3) {
      startPage = 1;
      endPage = 5;
    } else if (page >= totalPages - 2) {
      startPage = totalPages - 4;
      endPage = totalPages;
    } else {
      startPage = page - 2;
      endPage = page + 2;
    }

    return Array.from(
      { length: endPage - startPage + 1 },
      (_, i) => startPage + i
    );
  };

  // add/remove title to/from library
  const addToLibrary = async (manga) => {
    if (!user) {
      navigate("/login");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/library/add/${manga.id}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${user.token}`,
        },
      });

      if (!res.ok) throw new Error("Failed to add manga");
      const savedEntry = await res.json();

      // Update frontend copy
      setLibrary((prev) => [
        ...prev,
        { ...manga, readingStatus: savedEntry.readingStatus ?? "Plan to Read" },
      ]);
    } catch (err) {
      console.error(err);
      alert("Could not add manga to Library.");
    }
  };

  const removeFromLibrary = async (manga) => {
    if (!user) {
      navigate("/login");
      return;
    }

    if (!window.confirm("Remove this title from Library?")) return;

    try {
      const res = await fetch(`${API_BASE}/api/library/remove/${manga.id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });

      if (!res.ok) throw new Error("Failed to remove manga");
      setLibrary((prev) => prev.filter((item) => item.id !== manga.id));
    } catch (err) {
      console.error(err);
      alert("Could not remove manga from Library.");
    }
  };

  return (
    <div className="space-y-6">
      {id && <MangaPage basePath="/discover" />}

      <div className="text-center space-y-2">
        <h2 className="text-3xl font-bold text-logo">Discover New Titles</h2>
        <p className="text-foreground">Add to your Library, track as you go.</p>
      </div>

      <FilterBar />

      {/* Manga Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {mangaData.map((item) => (
          <div
            key={item.id}
            onClick={() => handleMangaClick(item)}
            className="bg-background rounded-lg shadow-sm border border-border overflow-hidden transition-all hover:shadow-lg hover:scale-105 cursor-pointer"
          >
            <div className="aspect-[9/10] overflow-hidden relative">
              <img
                src={
                  item.coverUrl
                    ? `${API_BASE}/api/covers/${item.id}`
                    : "/placeholder.svg"
                }
                alt={item.title}
                className="h-full w-full object-cover transition-transform hover:scale-110"
              />
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  if (isInLibrary(item.id)) {
                    removeFromLibrary(item);
                  } else {
                    addToLibrary(item);
                  }
                }}
                className={`absolute top-2 right-2 rounded-full p-1 shadow-md transition-colors
    ${
      isInLibrary(item.id)
        ? "bg-red-500 text-white hover:bg-red-600"
        : " text-white"
    }`}
              >
                {isInLibrary(item.id) ? (
                  <X className="h-4 w-4" />
                ) : (
                  <Plus className="h-4 w-4" />
                )}
              </button>
            </div>
            <div className="p-2">
              <h3 className="font-medium text-xs line-clamp-2 text-logo mb-1">
                {item.title}
              </h3>
              <p className="text-xs text-foreground mb-1">{item.author}</p>

              <div className="flex items-center gap-1 mb-1">
                <Star className="h-3 w-3 fill-star-yellow text-star-yellow" />
                <span className="text-xs">
                  {item.rating === null ? "N/A" : item.rating}
                </span>
              </div>

              <div className="flex flex-wrap gap-1">
                {item.genres.slice(0, 2).map((genre) => (
                  <span
                    key={genre}
                    className="text-xs px-1 py-0 bg-slight-tones text-foreground rounded"
                  >
                    {genre}
                  </span>
                ))}
                {item.genres.length > 2 && (
                  <span className="text-xs px-1 py-0 bg-slight-tones text-foreground rounded">
                    ...
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-center gap-2">
        <button
          onClick={() => handlePageChange(Math.max(1, page - 1))}
          disabled={page === 1}
          className="flex items-center gap-1 px-3 py-2 text-sm border border-border rounded-md hover:bg-slight-tones disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft className="h-4 w-4" />
          Previous
        </button>

        <div className="flex items-center gap-1">
          {getPageNumbers().map((pageNum) => (
            <button
              key={pageNum}
              onClick={() => handlePageChange(pageNum)}
              className={`w-8 h-8 text-sm rounded-md transition-colors ${
                page === pageNum
                  ? "bg-accent text-text-core"
                  : "border border-border hover:bg-slight-tones"
              }`}
            >
              {pageNum}
            </button>
          ))}
        </div>

        <button
          onClick={() => handlePageChange(Math.min(totalPages, page + 1))}
          disabled={page === totalPages}
          className="flex items-center gap-1 px-3 py-2 text-sm border border-border rounded-md hover:bg-slight-tones disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          Next
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}
