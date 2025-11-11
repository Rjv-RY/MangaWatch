export default function LibraryStats({ library }) {
  if (library.length === 0) return null;

  const total = library.length;
  const completed = library.filter(
    (m) => m.readingStatus === "Completed"
  ).length;
  const reading = library.filter((m) => m.readingStatus === "Reading").length;
  const avgRating =
    total > 0
      ? (
          library.reduce(
            (acc, m) => acc + (m.rating != null ? parseFloat(m.rating) : 0),
            0
          ) / total
        ).toFixed(1)
      : "0";

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
      <div className="bg-background p-4 rounded-lg border border-slight-tones-2 text-center">
        <div className="text-2xl font-bold text-chart-2">{total}</div>
        <div className="text-sm text-popover-foreground">Total Manga</div>
      </div>
      <div className="bg-background p-4 rounded-lg border border-slight-tones-2 text-center">
        <div className="text-2xl font-bold text-chart-2">{completed}</div>
        <div className="text-sm text-popover-foreground">Completed</div>
      </div>
      <div className="bg-background p-4 rounded-lg border border-slight-tones-2 text-center">
        <div className="text-2xl font-bold text-chart-2">{reading}</div>
        <div className="text-sm text-popover-foreground">Reading</div>
      </div>
      <div className="bg-background p-4 rounded-lg border border-slight-tones-2 text-center">
        <div className="text-2xl font-bold text-chart-2">{avgRating}</div>
        <div className="text-sm text-popover-foreground">Avg Rating</div>
      </div>
    </div>
  );
}
