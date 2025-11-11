// export default function Footer() {
//   return (
//     <footer className="bg-gray-800 text-white text-sm p-4 text-center">
//       <p>&copy; {new Date().getFullYear()} MangaWatch. All rights reserved.</p>
//     </footer>
//   );
// }

import { Link } from "react-router-dom";

export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="border-t border-border-content-side bg-background text-sm">
      <div className="mx-auto max-w-6xl px-4 py-6">
        <div className="flex flex-col items-center justify-between gap-4 md:flex-row">
          <p className="text-foreground font-bold">
            &copy; {year} MangaWatch. All rights reserved.
          </p>

          <nav
            aria-label="Footer"
            className="flex flex-wrap items-center gap-4 text-fforeground"
          >
            {/* Primary quick links */}
            <Link to="/" className="hover:text-primary">
              Home
            </Link>
            <Link to="/discover" className="hover:text-primary">
              Discover
            </Link>
            <Link to="/library" className="hover:text-primary">
              Library
            </Link>

            {/* Info */}
            <a href="#about" className="hover:text-primary">
              About
            </a>
            <a href="#contact" className="hover:text-primary">
              Contact
            </a>

            {/* Legal */}
            <a href="#privacy" className="hover:text-primary">
              Privacy
            </a>
            <a href="#terms" className="hover:text-primary">
              Terms
            </a>
          </nav>
        </div>
      </div>
    </footer>
  );
}
