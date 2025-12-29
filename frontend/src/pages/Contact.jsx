import PageContainer from "../components/PageContainer";

export default function Contact() {
  return (
    <PageContainer title="Contact">
      <section>
        <h2 className="text-xl font-semibold text-foreground">Get in touch</h2>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          For questions, bug reports, or account-related issues, you can reach
          out using the contact information below.
        </p>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-foreground">
          Contact methods
        </h2>
        <ul className="space-y-3">
          <li className="leading-relaxed">
            Email:{" "}
            <a
              href="mailto:rajivryadav18@gmail.com"
              className="text-chart-3 hover:underline underline-offset-2"
            >
              Email Support
            </a>
          </li>
          <li className="leading-relaxed">
            GitHub:{" "}
            <a
              href="https://github.com/Rjv-RY"
              target="_blank"
              rel="noreferrer"
              className="text-chart-3 hover:underline underline-offset-2"
            >
              github.com/Rjv-RY
            </a>
          </li>
        </ul>
        <p className="text-sm md:text-base text-popover-foreground leading-relaxed">
          Do visit the Github Repo and feel free to clone it as you see fit.
        </p>
      </section>
    </PageContainer>
  );
}
