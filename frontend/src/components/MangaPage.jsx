import { useEffect, useState, useContext } from "react";
import { API_BASE } from "../config/api";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { X, Star, Plus, Trash2 } from "lucide-react";
import { LibraryContext } from "../context/LibraryContext";
import { useAuth } from "../context/AuthContext";

export default function MangaPage({ basePath = "/discover" }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { library, setLibrary } = useContext(LibraryContext);
  const { user } = useAuth();
  const [manga, setManga] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const isInLibrary = manga
    ? library.some((item) => item.id === manga.id)
    : false;

  useEffect(() => {
    if (!id) return;

    const fetchMangaDetails = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${API_BASE}/api/manga/${id}`);
        if (!response.ok) throw new Error("Failed to fetch manga details");
        const data = await response.json();
        setManga(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchMangaDetails();
  }, [id]);

  const location = useLocation();
  const handleClose = () => {
    const search = location.search || "";
    navigate(`${basePath}${search}`);
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
      alert("could not add manga to Library.");
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

  const handleRate = () =>
    alert("Rating functionality will be implemented later");

  if (!id) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-lg"
      onClick={handleClose}
    >
      <div
        className="relative w-full max-w-5xl max-h-[90vh] overflow-y-auto bg-background/90 rounded-lg shadow-2xl border border-border m-4"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close Button */}
        <button
          onClick={handleClose}
          className="absolute top-4 right-4 z-10 p-2 bg-background/80 hover:bg-muted rounded-full transition-colors border border-border"
        >
          <X className="h-5 w-5 text-foreground" />
        </button>

        {loading && (
          <div className="flex items-center justify-center p-12">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
              <p className="text-muted-foreground">Loading manga details...</p>
            </div>
          </div>
        )}

        {error && (
          <div className="flex items-center justify-center p-12">
            <div className="text-center">
              <p className="text-destructive mb-4">Error: {error}</p>
              <button
                onClick={handleClose}
                className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        )}

        {manga && !loading && !error && (
          <div className="p-6">
            {/* Top Section: Cover + Details */}
            <div className="flex flex-col md:flex-row gap-6 mb-6">
              <div className="flex-shrink-0">
                <img
                  src={
                    manga.coverUrl
                      ? `${API_BASE}/api/covers/${manga.id}`
                      : "/placeholder.svg"
                  }
                  alt={manga.title}
                  className="w-full md:w-64 h-auto rounded-lg shadow-lg border border-border object-cover"
                />
              </div>

              <div className="flex-1 space-y-4">
                <div>
                  <h1 className="text-3xl font-bold text-foreground mb-2">
                    {manga.title}
                  </h1>
                  <p className="text-lg text-muted-foreground">
                    by {manga.author}
                  </p>
                </div>

                <div className="flex flex-wrap gap-4 text-sm">
                  <div>
                    <span className="text-muted-foreground">Year: </span>
                    <span className="text-foreground font-medium">
                      {manga.year}
                    </span>
                  </div>
                  <div>
                    <span className="text-muted-foreground">Status: </span>
                    <span
                      className={`font-medium ${
                        manga.status === "Ongoing"
                          ? "text-chart-2"
                          : "text-chart-1"
                      }`}
                    >
                      {manga.status}
                    </span>
                  </div>
                </div>

                {/* Rating */}
                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-2 bg-muted px-3 py-2 rounded-md">
                    <Star className="h-5 w-5 fill-star-yellow text-star-yellow" />
                    <span className="text-lg font-semibold text-foreground">
                      {manga.rating ?? "N/A"}
                    </span>
                  </div>
                  <button
                    onClick={handleRate}
                    className="px-4 py-2 text-sm border border-border rounded-md hover:bg-muted transition-colors"
                  >
                    Rate
                  </button>
                </div>

                {/* Genres */}
                {manga.genres && manga.genres.length > 0 && (
                  <div>
                    <h3 className="text-sm font-semibold text-muted-foreground mb-2">
                      Genres
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {manga.genres.map((genre) => (
                        <span
                          key={genre}
                          className="px-3 py-1 bg-muted text-foreground text-sm rounded-md border border-border"
                        >
                          {genre}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Add/Remove from Library Button */}
                <div className="pt-2">
                  {isInLibrary ? (
                    <button
                      onClick={() => removeFromLibrary(manga)}
                      className="flex items-center gap-2 px-6 py-3 bg-destructive text-destructive-foreground rounded-md hover:bg-destructive/90 transition-colors font-medium"
                    >
                      <Trash2 className="h-4 w-4" />
                      Remove from Library
                    </button>
                  ) : (
                    <button
                      onClick={() => addToLibrary(manga)}
                      className="flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors font-medium"
                    >
                      <Plus className="h-4 w-4" />
                      Add to Library
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Description */}
            {manga.description && (
              <div className="mb-6">
                <h2 className="text-lg font-semibold text-foreground mb-2">
                  Description
                </h2>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  {manga.description}
                </p>
              </div>
            )}

            {/* Alternate Titles */}
            {manga.altTitles && manga.altTitles.length > 0 && (
              <div className="mb-6">
                <h2 className="text-lg font-semibold text-foreground mb-2">
                  Alternate Titles
                </h2>
                <p className="text-sm text-muted-foreground">
                  {manga.altTitles.join(", ")}
                </p>
              </div>
            )}

            {/* Future Reviews/Comments Section */}
            <div className="border-t border-border pt-6">
              <h2 className="text-xl font-semibold text-foreground mb-3">
                Reviews & Comments
              </h2>
              <p className="text-muted-foreground italic">Coming soon...</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
