import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import { LibraryProvider } from "./context/LibraryContext.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>
      <LibraryProvider>
        <App />
      </LibraryProvider>
    </AuthProvider>
  </StrictMode>
);
