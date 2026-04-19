import { PageHeader } from "../components/common/PageHeader";
import { PlaceholderPanel } from "../components/common/PlaceholderPanel";

export function UploadDocumentPage() {
  return (
    <div className="page-shell">
      <PageHeader
        eyebrow="Documents"
        title="Upload reports for AI-assisted explanation"
        description="This route is reserved for file upload, processing status, extracted summaries, and category-level product relevance guidance. It should remain clearly scoped to explanation, not diagnosis."
      />

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <PlaceholderPanel
          title="Upload panel"
          description="Add drag-and-drop upload, file validation, and async processing state here."
          bullets={[
            "Accepted files: PDF, image, or text-based reports.",
            "Show upload progress and processing status.",
            "Keep disclaimers visible in the UI.",
          ]}
        />
        <PlaceholderPanel
          title="Extraction result panel"
          description="Add plain-language summary, extracted themes, and broad product category relevance once the backend document flow is ready."
        />
      </div>
    </div>
  );
}
