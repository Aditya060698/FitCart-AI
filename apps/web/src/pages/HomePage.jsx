import { MetricCard } from "../components/common/MetricCard";
import { PageHeader } from "../components/common/PageHeader";
import { PlaceholderPanel } from "../components/common/PlaceholderPanel";

export function HomePage() {
  return (
    <div className="space-y-6">
      <section className="page-shell">
        <PageHeader
          eyebrow="Product Vision"
          title="An AI-guided nutrition shopping experience"
          description="FitCart AI should feel like decision-support commerce, not a generic chatbot. This home page starter is shaped around the core journeys: discovery, comparison, understanding, advice, and document-assisted guidance."
        />

        <div className="mt-8 grid gap-4 md:grid-cols-3">
          <MetricCard label="Primary loop" value="Browse → Compare → Decide" helper="The frontend should reinforce this decision loop on every major screen." />
          <MetricCard label="AI pattern" value="Grounded assistance" helper="Use AI for explanation and reasoning, not deterministic catalog math." />
          <MetricCard label="Frontend style" value="Feature-first" helper="Pages, components, API clients, and hooks are organized to scale without becoming a folder dump." />
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <PlaceholderPanel
          title="Core journeys wired into the architecture"
          description="The starter route structure already matches the product surface area: listing, detail, compare, advisor, upload, and saved products."
          bullets={[
            "Product listing page for search, filters, and pagination.",
            "Product details page for ingredient, review, and explanation flows.",
            "AI advisor page for goal-driven recommendation sessions.",
            "Upload page for document understanding and extracted summaries.",
          ]}
        />
        <PlaceholderPanel
          title="Server state strategy"
          description="TanStack Query is configured as the default server-state layer. It will own fetching, caching, loading states, and stale-data behavior for backend-backed views."
          bullets={[
            "Axios owns HTTP configuration and base URLs.",
            "React Router owns page navigation and route params.",
            "Feature hooks wrap queries to keep page components clean.",
          ]}
        />
      </div>
    </div>
  );
}
