import PageContainer from "../components/PageContainer";

export default function About() {
  return (
    <PageContainer title="About MangaWatch">
      <section>
        <h2 className="text-xl font-semibold text-foreground">
          What is MangaWatch?
        </h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          MangaWatch is a personal manga tracking application that allows users
          to manage reading progress and maintain a private library of manga.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          What you can do
        </h2>
        <ul className="space-y-2 text-sm md:text-base text-popover-foreground">
          <li className="leading-relaxed">
            Track manga you are currently reading.
          </li>
          <li className="leading-relaxed">Maintain a personal reading list.</li>
          <li className="leading-relaxed">
            View manga metadata sourced from public APIs, see what interests
            you.
          </li>
        </ul>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">Data sources</h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          Manga metadata and cover images are sourced from the MangaDex public
          API. MangaWatch is not affiliated with, endorsed by, or operated by
          MangaDex or any rights holders in any way. This may change in the
          future if I change to a different database. The sourcing part
          specifically.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          Project status
        </h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          This project is developed as a personal and portfolio project. It is
          non-commercial and may change or be discontinued at any time.
          Hopefully though It keeps growing and improving.
        </p>
      </section>
    </PageContainer>
  );
}
