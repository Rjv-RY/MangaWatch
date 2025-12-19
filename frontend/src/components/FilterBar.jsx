import { useState, useEffect } from "react";
import { API_BASE } from "../config/api";
import { ChevronDown, X } from "lucide-react";
import { useDiscoverParams } from "../hooks/useDiscoverParams";
import { useRef } from "react";

// Keep STATUS_OPTIONS hardcoded (they're fixed)
const STATUS_OPTIONS = ["Ongoing", "Completed", "Hiatus"];

const SORT_OPTIONS = [
  { value: "title", label: "Title" },
  { value: "author", label: "Author" },
  { value: "rating", label: "Rating" },
  { value: "year", label: "Year" },
];

export default function FilterBar() {
  const firstRun = useRef(true);
  const { genres, status, sort, updateParams } = useDiscoverParams();
  const [selectedGenres, setSelectedGenres] = useState(genres);
  const [selectedStatus, setSelectedStatus] = useState(status);
  const [showGenres, setShowGenres] = useState(false);
  const [showStatus, setShowStatus] = useState(false);
  const [sortBy, setSortBy] = useState(sort);
  // Fetch genres from backend
  const [genreOptions, setGenreOptions] = useState([]);
  const [isLoadingGenres, setIsLoadingGenres] = useState(true);

  //Fetch genres on component mount
  useEffect(() => {
    const fetchGenres = async () => {
      try {
        const response = await fetch(`${API_BASE}/api/manga/genres`);
        const genres = await response.json();
        setGenreOptions(genres); // Already sorted alphabetically from backend
        setIsLoadingGenres(false);
      } catch (error) {
        console.error("Failed to fetch genres:", error);
        setIsLoadingGenres(false);
        // Fallback to empty array or show error
      }
    };
    fetchGenres();
  }, []);

  // sync local control state when URL changes (no set of URL here)
  useEffect(() => {
    setSelectedGenres(genres);
    setSelectedStatus(status);
    setSortBy(sort);
  }, [genres, status, sort]);

  // Updates URL only when user changes filters (not when URL changes itself)
  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false;
      return;
    }

    const equalArrays = (a, b) =>
      a.length === b.length && a.every((v) => b.includes(v));

    // Only trigger URL update if *user-driven* state differs from URL
    const hasChanged =
      !equalArrays(selectedGenres, genres) ||
      !equalArrays(selectedStatus, status) ||
      sortBy !== sort;

    if (hasChanged) {
      updateParams({
        genres: selectedGenres,
        status: selectedStatus,
        sort: sortBy,
        page: 1,
      });
    }
    // ⚠️ Only include user-controlled states — avoid recursive triggers
  }, [selectedGenres, selectedStatus, sortBy]);

  const toggleGenre = (genre) => {
    setSelectedGenres((prev) =>
      prev.includes(genre) ? prev.filter((g) => g !== genre) : [...prev, genre]
    );
  };

  const toggleStatus = (status) => {
    setSelectedStatus((prev) =>
      prev.includes(status)
        ? prev.filter((s) => s !== status)
        : [...prev, status]
    );
  };

  const clearFilters = () => {
    setSelectedGenres([]);
    setSelectedStatus([]);
    setSortBy("title");
  };

  const hasActiveFilters =
    selectedGenres.length > 0 ||
    selectedStatus.length > 0 ||
    sortBy !== "title";

  return (
    <div className="space-y-3">
      {/* Filter Controls */}
      <div className="flex flex-wrap items-center gap-3">
        {/* Sort Dropdown */}
        <div className="relative">
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="appearance-none bg-background border border-border rounded-md px-3 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-register-btn focus:border-register-btn"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                Sort: {option.label}
              </option>
            ))}
          </select>
          <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-search-highlight pointer-events-none" />
        </div>

        {/* Status Filter */}
        <div className="relative">
          <button
            onClick={() => setShowStatus(!showStatus)}
            className="flex items-center gap-2 bg-background border border-border rounded-md px-3 py-2 text-sm hover:bg-slight-tones focus:outline-none focus:ring-2 focus:ring-register-btn"
          >
            Status
            {selectedStatus.length > 0 && ` (${selectedStatus.length})`}
            <ChevronDown className="h-4 w-4 text-search-highlight" />
          </button>
          {showStatus && (
            <div className="absolute top-full left-0 mt-1 bg-background border border-border rounded-md shadow-lg z-10 min-w-[150px]">
              {STATUS_OPTIONS.map((status) => (
                <label
                  key={status}
                  className="flex items-center gap-2 px-3 py-2 hover:bg-slight-tones cursor-pointer"
                >
                  <input
                    type="checkbox"
                    checked={selectedStatus.includes(status)}
                    onChange={() => toggleStatus(status)}
                    className="rounded border-border text-chart-2 focus:ring-register-btn"
                  />
                  <span className="text-sm">{status}</span>
                </label>
              ))}
            </div>
          )}
        </div>

        {/* Genres Filter */}
        <div className="relative">
          <button
            onClick={() => setShowGenres(!showGenres)}
            className="flex items-center gap-2 bg-background border border-border rounded-md px-3 py-2 text-sm hover:bg-slight-tones focus:outline-none focus:ring-2 focus:ring-register-btn"
          >
            Genres
            {selectedGenres.length > 0 && ` (${selectedGenres.length})`}
            <ChevronDown className="h-4 w-4 text-search-highlight" />
          </button>
          {showGenres && (
            <div className="absolute top-full left-0 mt-1 bg-background border border-border rounded-md shadow-lg z-10 min-w-[200px] max-h-60 overflow-y-auto">
              {isLoadingGenres ? (
                <div className="px-3 py-2 text-sm text-gray-500">
                  Loading genres...
                </div>
              ) : genreOptions.length === 0 ? (
                <div className="px-3 py-2 text-sm text-gray-500">
                  No genres available
                </div>
              ) : (
                genreOptions.map((genre) => (
                  <label
                    key={genre}
                    className="flex items-center gap-2 px-3 py-2 hover:bg-slight-tones cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      checked={selectedGenres.includes(genre)}
                      onChange={() => toggleGenre(genre)}
                      className="rounded border-border text-chart-2 focus:ring-register-btn"
                    />
                    <span className="text-sm">{genre}</span>
                  </label>
                ))
              )}
            </div>
          )}
        </div>

        {/* Clear Filters */}
        {hasActiveFilters && (
          <button
            onClick={clearFilters}
            className="text-sm text-sidebar-foreground hover:text-logo underline"
          >
            Clear Filters
          </button>
        )}
      </div>

      {/* Active Filter Tags */}
      {(selectedGenres.length > 0 || selectedStatus.length > 0) && (
        <div className="flex flex-wrap gap-2">
          {selectedGenres.map((genre) => (
            <span
              key={genre}
              className="inline-flex items-center gap-1 bg-emer-prpl-lite text-primary text-xs px-2 py-1 rounded-full"
            >
              {genre}
              <button
                onClick={() => toggleGenre(genre)}
                className="hover:bg-content-bg rounded-full p-0.5"
              >
                <X className="h-3 w-3" />
              </button>
            </span>
          ))}
          {selectedStatus.map((status) => (
            <span
              key={status}
              className="inline-flex items-center gap-1 bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full"
            >
              {status}
              <button
                onClick={() => toggleStatus(status)}
                className="hover:bg-blue-200 rounded-full p-0.5"
              >
                <X className="h-3 w-3" />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
