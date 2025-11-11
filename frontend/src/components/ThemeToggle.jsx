import { useState, useEffect } from "react";
import { Sun, Moon } from "lucide-react";

export function ThemeToggle() {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    // Load theme from localStorage or system preference
    const stored = localStorage.getItem("theme");
    if (stored === "dark") {
      document.documentElement.classList.add("dark");
      setIsDark(true);
    } else if (stored === "light") {
      document.documentElement.classList.remove("dark");
      setIsDark(false);
    } else {
      // fallback: check system preference
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
      if (prefersDark) {
        document.documentElement.classList.add("dark");
        setIsDark(true);
      }
    }
  }, []);

  const toggleTheme = () => {
    setIsDark((prev) => {
      const next = !prev;
      if (next) {
        document.documentElement.classList.add("dark");
        localStorage.setItem("theme", "dark");
      } else {
        document.documentElement.classList.remove("dark");
        localStorage.setItem("theme", "light");
      }
      return next;
    });
  };

  return (
    <button
      onClick={toggleTheme}
      className={`relative inline-flex h-8 w-14 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 ${
        isDark ? "bg-border" : "bg-background border border-border"
      }`}
      aria-label="Toggle theme"
    >
      <span
        className={`inline-block h-6 w-6 transform rounded-full transition-transform duration-200 ${
          isDark ? "translate-x-8 bg-logo" : "translate-x-1 bg-slight-tones"
        }`}
      >
        {isDark ? (
          <Moon className="h-5 w-4 text-moon-purple m-0.5" />
        ) : (
          <Sun className="h-5 w-4 text-star-yellow m-0.5" />
        )}
      </span>
    </button>
  );
}
