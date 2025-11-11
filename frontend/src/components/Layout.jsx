import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Footer from "./Footer";

export default function Layout() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="bg-content-bg max-w-[clamp(320px,80vw,1280px)] w-full mx-auto px-4 flex-grow">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
