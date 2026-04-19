import { PageHeader } from "../components/common/PageHeader";
import { PlaceholderPanel } from "../components/common/PlaceholderPanel";
import { AdvisorPromptCard } from "../features/ai/components/AdvisorPromptCard";
import { env } from "../lib/config/env";

export function AiAdvisorPage() {
  return (
    <div className="space-y-6">
      <section className="page-shell">
        <PageHeader
          eyebrow="AI Advisor"
          title="Goal-driven recommendation interface"
          description="The advisor should collect user intent, send a structured request to the backend orchestration layer, and then render grounded recommendations with transparent reasoning."
        />

        <div className="mt-8 grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <AdvisorPromptCard />
          <PlaceholderPanel
            title="Integration notes"
            description={`Browser-facing AI service base URL: ${env.aiBaseUrl}. In the production shape, the frontend will usually call Spring Boot first, and Spring Boot will orchestrate the AI service behind the scenes.`}
            bullets={[
              "Prompt capture and validation live in the page and feature layer.",
              "Server state for responses should use TanStack Query or mutations.",
              "Returned reasoning should be structured, not free-form only.",
            ]}
          />
        </div>
      </section>
    </div>
  );
}
