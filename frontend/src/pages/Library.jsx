import { useState, useContext } from "react";
import { API_BASE } from "../config/api";
import { useNavigate, useParams } from "react-router-dom";
import { Star, Filter, X, ChevronDown } from "lucide-react";
import { LibraryContext } from "../context/LibraryContext";
import LibraryStats from "../components/LibraryStats";
import EmptyLibrary from "../components/EmptyLibrary";
import MangaPage from "../components/MangaPage";

export default function Library() {
  const [sortBy, setSortBy] = useState("popularity");
  const [filterStatus, setFilterStatus] = useState("all");
  const navigate = useNavigate();
  const { dexId } = useParams();

  const { library, setLibrary, loading } = useContext(LibraryContext);

  const STATUSES = [
    { value: "Reading", label: "Reading" },
    { value: "Completed", label: "Completed" },
    { value: "Plan to Read", label: "Plan to Read" },
  ];

  const filteredLibrary = library.filter((item) => {
    if (filterStatus === "all") return true;
    return item.readingStatus === filterStatus;
  });

  const sortedLibrary = [...filteredLibrary].sort((a, b) => {
    switch (sortBy) {
      case "popularity":
        return b.rating - a.rating;
      case "date":
        return b.year - a.year;
      case "title":
        return a.title.localeCompare(b.title);
      default:
        return 0;
    }
  });

  const handleMangaClick = (item) => {
    navigate(`/library/${item.dexId}`);
  };

  //changing reading status
  const cycleStatus = async (id) => {
    setLibrary((prev) =>
      prev.map((item) => {
        const currentIndex = STATUSES.findIndex(
          (s) => s.label === item.readingStatus
        );
        const nextStatus = STATUSES[(currentIndex + 1) % STATUSES.length].label;
        if (item.id === id) {
          updateReadingStatusOnServer(id, nextStatus); // async call
          return { ...item, readingStatus: nextStatus };
        }
        return item;
      })
    );
  };

  const updateReadingStatusOnServer = async (id, newStatus) => {
    const token = localStorage.getItem("manga_token");

    try {
      console.log(import.meta.env);
      console.log(API_BASE);
      console.log(import.meta.env.VITE_API_BASE_URL);
      const response = await fetch(
        `${API_BASE}/api/library/update-status/${id}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ readingStatus: newStatus }),
        }
      );

      if (!response.ok) {
        console.error("Failed to update reading status:", response.status);
      }
    } catch (err) {
      console.error("Error updating reading status:", err);
    }
  };

  //removing title
  const removeTitle = async (id) => {
    const confirmed = window.confirm("Remove this title from Library?");
    if (!confirmed) return;

    const token = localStorage.getItem("manga_token");

    try {
      const response = await fetch(`${API_BASE}/api/library/remove/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        //removed successfully
        setLibrary((prev) => prev.filter((item) => item.id !== id));
      } else {
        console.error("Failed to remove manga:", response.status);
      }
    } catch (err) {
      console.error("Error removing manga:", err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Section */}
      {dexId && <MangaPage basePath="/library" />}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-3xl font-bold text-logo">Your Library</h2>
          <p className="text-popover-foreground">
            Your personal manga collection
          </p>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-chart-2" />
            <div className="relative">
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="appearance-none bg-background border border-border rounded-md px-3 py-2 pr-8 text-sm hover:bg-slight-tones focus:outline-none focus:ring-2 focus:ring-register-btn focus:border-register-btn"
              >
                <option value="all">All Status</option>
                {STATUSES.map((s) => (
                  <option key={s.value} value={s.value}>
                    {s.label}
                  </option>
                ))}
              </select>
              <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-search-highlight pointer-events-none" />
            </div>
          </div>

          <div className="relative">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="appearance-none bg-background border border-border rounded-md px-3 py-2 pr-8 text-sm hover:bg-slight-tones focus:outline-none focus:ring-2 focus:ring-register-btn focus:border-register-btn"
            >
              <option value="popularity">Sort: Rating</option>
              <option value="date">Sort: Date Added</option>
              <option value="title">Sort: Title</option>
            </select>
            <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-search-highlight pointer-events-none" />
          </div>
        </div>
      </div>

      {/* Empty State or Library Grid */}
      {loading ? (
        <div className="p-6">Loading library…</div>
      ) : sortedLibrary.length === 0 ? (
        <EmptyLibrary />
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
          {sortedLibrary.map((item) => (
            <div
              key={item.id}
              className="bg-background rounded-lg shadow-sm border border-slight-tones-2 overflow-hidden transition-all hover:shadow-lg hover:scale-105"
              onClick={() => handleMangaClick(item)}
            >
              <div className="aspect-[4/5] overflow-hidden">
                <img
                  src={item.coverUrl || "/placeholder.svg"}
                  alt={item.title}
                  className="h-full w-full object-cover transition-transform hover:scale-110"
                />
                {/*remove btn */}
                <button
                  onClick={() => removeTitle(item.id)}
                  className="absolute top-2 right-2 bg-red-500/80 hover:bg-red-600 text-white rounded-full p-1 transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <div className="p-3">
                <h3 className="font-semibold text-sm line-clamp-2 text-logo mb-1">
                  {item.title}
                </h3>
                <p className="text-xs text-popover-foreground mb-2">
                  {item.author}
                </p>

                <div className="flex items-center gap-1 mb-2">
                  <Star className="h-3 w-3 fill-star-yellow text-star-yellow" />
                  <span className="text-xs font-medium">{item.rating}</span>
                </div>

                <div className="flex flex-wrap gap-1 mb-3">
                  <span
                    className={`text-xs px-2 py-1 rounded ${
                      item.readingStatus === "Reading"
                        ? "bg-content-bg text-foreground"
                        : item.readingStatus === "Completed"
                        ? "bg-slight-tones text-foreground"
                        : "bg-blue-100 text-blue-800"
                    }`}
                  >
                    {item.readingStatus}
                  </span>
                </div>

                <button
                  className="w-full px-3 py-2 text-xs border border-border rounded-md hover:bg-slight-tones transition-colors"
                  onClick={() => cycleStatus(item.id)}
                >
                  Update Status
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Stats Section */}
      {sortedLibrary.length > 0 && <LibraryStats library={library} />}
    </div>
  );
}
