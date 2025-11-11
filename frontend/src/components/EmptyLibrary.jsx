import { useNavigate } from "react-router-dom";

export default function EmptyLibrary() {
  const navigate = useNavigate();
  return (
    <div className="text-center py-12 space-y-4">
      <div className="text-6xl">📚</div>
      <h3 className="text-xl font-semibold text-logo">
        Your library is empty here
      </h3>
      <p className="text-popover-foreground">
        Add manga from the Discover page to build your Library up?
      </p>
      <button
        onClick={() => navigate("/discover")}
        className="inline-flex items-center gap-2 px-4 py-2 bg-chart-2 text-text-core rounded-md hover:bg-secondary transition-colors"
      >
        Discover Manga +
      </button>
    </div>
  );
}
