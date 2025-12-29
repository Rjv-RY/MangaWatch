export default function PageContainer({ title, children }) {
  return (
    <main className="page-container max-w-3xl mx-auto px-4 py-12 space-y-10">
      <h1 className="page-title text-3xl md:text-4xl font-bold text-logo">
        {title}
      </h1>
      {children}
    </main>
  );
}
