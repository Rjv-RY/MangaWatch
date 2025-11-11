import { Search } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { Link } from "react-router-dom";

const popularGenres = [
  "Action",
  "Adventure",
  "Comedy",
  "Crime",
  "Drama",
  "Fantasy",
  "Historical",
  "Horror",
  "Martial Arts",
  "Mecha",
  "Mystery",
  "Psychological",
  "Romance",
  "Sci-Fi",
  "Seinen",
  "Shounen",
  "Slice of Life",
  "Sports",
  "Supernatural",
  "Thriller",
];

export default function Home() {
  //handle search at homepage
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();

  const handleSearch = (e) => {
    if (e.key === "Enter" && searchTerm.trim() !== "") {
      navigate(`/discover?query=${encodeURIComponent(searchTerm.trim())}`);
    }
  };
  //handle search at homepage

  return (
    <div className="max-w-4xl mx-auto space-y-12">
      {/* Hero Section */}
      <div className="text-center space-y-6">
        <div className="space-y-4">
          <h1 className="text-4xl md:text-6xl font-bold text-logo">
            Welcome to MangaWatch
          </h1>
          <p className="text-lg md:text-xl text-popover-foreground max-w-2xl mx-auto">
            Track your reading progress, discover new series, and connect with
            other readers.
          </p>
        </div>

        {/* Search Bar */}
        <div className="relative max-w-2xl mx-auto">
          <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-search-highlight" />
          <input
            placeholder="Search for manga titles, authors, or genres..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={handleSearch}
            className="w-full pl-12 pr-4 py-6 text-lg border-2 border-slight-tones-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-register-btn focus:border-accent transition-colors"
          />
        </div>
      </div>

      {/* Popular Genres */}
      <div className="space-y-6">
        <h2 className="text-2xl font-semibold text-center text-logo">
          Pick a Genre
        </h2>
        <div className="flex flex-wrap justify-center gap-3">
          {popularGenres.map((genre) => (
            <Link
              key={genre}
              to={`/discover?genres=${encodeURIComponent(genre)}`}
            >
              <button
                key={genre}
                className="px-4 py-2 text-sm border border-border rounded-full hover:bg-accent hover:text-text-core hover:border-primary transition-colors cursor-pointer"
              >
                {genre}
              </button>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
