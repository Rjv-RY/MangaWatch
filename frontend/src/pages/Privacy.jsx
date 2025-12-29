import PageContainer from "../components/PageContainer";

export default function Privacy() {
  return (
    <PageContainer title="Privacy Policy">
      <p className="text-xs text-muted-foreground">
        Last updated: December 2025
      </p>

      <section>
        <h2 className="text-xl font-semibold text-foreground">Overview</h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          MangaWatch collects minimal information required to provide basic
          authentication and user-specific features. No tracking or analytics
          are used by the application itself.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          Information we collect
        </h2>
        <ul className="space-y-2 text-sm md:text-base text-popover-foreground">
          <li className="leading-relaxed">
            <strong>Email address</strong> — used for account identification and
            login.
          </li>
          <li className="leading-relaxed">
            <strong>Password</strong> — stored only in hashed and salted form.
            Plaintext passwords are never stored.
          </li>
        </ul>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          Auth and Storage
        </h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          Authentication is handled using JSON Web Tokens (JWT), which is
          currently stored in localStorage to maintain session state.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          What we don't collect
        </h2>
        <ul className="space-y-2 text-sm md:text-base text-popover-foreground">
          <li className="leading-relaxed">
            The application itself does not implement tracking or analytics.
          </li>
          <li className="leading-relaxed">
            The application doesn't collect advertising data.
          </li>
          <li className="leading-relaxed">
            No third party data sharing from the application's side.
          </li>
        </ul>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">Data deletion</h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          Users may request deletion of their account and associated data by
          contacting the developer through the Contact page. I'll implement a
          proper data deletion flow to allow easier deletion.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          Changes to this policy
        </h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          This privacy policy may be updated periodically. Continued use of the
          application implies acceptance of any changes, or contact me to
          challenge the decisions you don't agree with or feel are wrong.
        </p>
      </section>
    </PageContainer>
  );
}
