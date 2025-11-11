import { Link, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { Search, User, Menu, BookOpen } from "lucide-react";
import { ThemeToggle } from "./ThemeToggle";
import { useDiscoverParams } from "../hooks/useDiscoverParams";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const navigate = useNavigate();
  const { user, logout: authLogout } = useAuth();
  const handleLogout = () => {
    authLogout();
    navigate("/login");
  };

  {
    /* handling search from here*/
  }
  //getting search queries from home page as well
  const { query, setParam } = useDiscoverParams();
  const [searchTerm, setSearchTerm] = useState(query);

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      const newQuery = searchTerm.trim();

      // build params first
      setParam("query", newQuery);
      setParam("page", 1);

      // navigate *to the current params*, not plain /discover
      navigate({
        pathname: "/discover",
        search: `?query=${encodeURIComponent(newQuery)}&sort=title&page=1`,
      });
    }
  };

  useEffect(() => {
    setSearchTerm(query);
  }, [query]);
  {
    /* handling search till here*/
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border-content-side bg-background backdrop-blur">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        {/* Logo and Title */}
        <div className="flex items-center gap-2">
          <BookOpen className="h-8 w-8 text-primary" />
          <h1 className="text-2xl font-bold text-logo">MangaWatch</h1>
        </div>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center gap-6">
          <Link
            to="/"
            className="text-left px-3 py-2 text-foreground hover:text-primary transition-colors"
          >
            Home
          </Link>
          <Link
            to="/library"
            className="text-left px-3 py-2 text-foreground hover:text-primary transition-colors"
          >
            Library
          </Link>
          <Link
            to="/discover"
            className="px-3 py-2 text-foreground hover:text-primary transition-colors"
          >
            Discover
          </Link>
        </nav>

        {/* Search Bar */}
        <div className="hidden md:flex items-center gap-4 flex-1 max-w-md mx-8">
          <div className="relative w-full">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-search-highlight" />
            <input
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={handleSearch}
              placeholder="Search for..."
              className="w-full pl-10 pr-4 py-2 bg-muted border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-accent focus:border-transparent"
            />
          </div>
        </div>

        {/* User Actions */}
        <div className="flex items-center gap-2">
          {user ? (
            <>
              <span className="hidden md:inline text-m text-foreground">
                Hello, {user.username}
              </span>
              <button
                onClick={handleLogout}
                className="hidden md:inline-flex px-4 py-2 text-sm border border-border rounded-md hover:bg-muted transition-colors"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="hidden md:inline-flex px-4 py-2 text-sm border border-border rounded-md hover:bg-muted transition-colors"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="hidden md:inline-flex px-4 py-2 text-sm bg-register-btn text-text-core rounded-md hover:bg-primary transition-colors"
              >
                Register
              </Link>
            </>
          )}

          {/* User Menu */}
          <button className="p-2 hover:bg-slight-tones rounded-md transition-colors">
            <User className="h-5 w-5" />
          </button>
          <ThemeToggle />
          {/* Mobile Menu */}
          <button
            className="md:hidden p-2 hover:bg-slight-tones rounded-md transition-colors"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            <Menu className="h-5 w-5" />
          </button>
        </div>
      </div>

      {/* Mobile Navigation */}
      {isMenuOpen && (
        <div className="md:hidden border-t bg-background">
          <div className="container mx-auto px-4 py-4 space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-search-highlight" />
              <input
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={handleSearch}
                placeholder="Search manga..."
                className="w-full pl-10 pr-4 py-2 bg-slight-tones border border-slight-tones-2 rounded-md focus:outline-none focus:ring-2 focus:ring-accent"
              />
            </div>
            <div className="flex gap-2 pt-2">
              {user ? (
                <>
                  <span className="text-m text-foreground">
                    Hello, {user.username}
                  </span>
                  <button
                    onClick={handleLogout}
                    className="px-4 py-2 text-sm border border-border rounded-md hover:bg-muted transition-colors"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="px-4 py-2 text-sm border border-border rounded-md hover:bg-muted transition-colors"
                  >
                    Login
                  </Link>
                  <Link
                    to="/register"
                    className="px-4 py-2 text-sm bg-register-btn text-text-core rounded-md hover:bg-primary transition-colors"
                  >
                    Register
                  </Link>
                </>
              )}
            </div>
            <div className="flex flex-col gap-2">
              <Link
                to="/"
                className="text-left px-3 py-2 text-foreground hover:text-primary transition-colors"
              >
                Home
              </Link>
              <Link
                to="/library"
                className="text-left px-3 py-2 text-foreground hover:text-primary transition-colors"
              >
                Library
              </Link>
              <Link
                to="discover"
                className="text-left px-3 py-2 text-foreground hover:text-primary transition-colors"
              >
                Discover
              </Link>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
