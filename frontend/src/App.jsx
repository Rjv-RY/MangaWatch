import "./App.css";
import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import Layout from "./components/Layout";
import Home from "./pages/Home";
import Library from "./pages/Library";
import Discover from "./pages/Discover";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MangaPage from "./components/MangaPage";
import ProtectedRoute from "./components/ProtectedRoute";

function AppRoutes() {
  const location = useLocation();
  const state = location.state; // checks if we came here from Discover
  const background = state && state.background;

  return (
    <>
      <Routes location={background || location}>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />

          <Route
            path="/library"
            element={
              <ProtectedRoute>
                <Library />{" "}
              </ProtectedRoute>
            }
          />
          <Route
            path="/library/:id"
            element={
              <ProtectedRoute>
                <Library />
              </ProtectedRoute>
            }
          />
          <Route path="/discover" element={<Discover />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>
      </Routes>

      {/* render the modal over the background */}
      {background && (
        <Routes>
          <Route path="/discover/:id" element={<MangaPage />} />
        </Routes>
      )}
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
