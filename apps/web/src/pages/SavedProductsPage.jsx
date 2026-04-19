import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { PageHeader } from "../components/common/PageHeader";
import { PlaceholderPanel } from "../components/common/PlaceholderPanel";
import { fetchSavedProducts, fetchSearchHistory } from "../lib/api/personalization";
import { demoUserReference } from "../lib/personalization/user";

export function SavedProductsPage() {
  const { data: savedProducts = [], isLoading: savedLoading } = useQuery({
    queryKey: ["saved-products", demoUserReference],
    queryFn: () => fetchSavedProducts(demoUserReference),
  });

  const { data: searchHistory = [], isLoading: historyLoading } = useQuery({
    queryKey: ["search-history", demoUserReference],
    queryFn: () => fetchSearchHistory(demoUserReference),
  });

  return (
    <div className="space-y-8 page-shell">
      <PageHeader
        eyebrow="Saved"
        title="Saved products and search memory"
        description="This route now surfaces the lightweight personalization layer for the current demo user: saved products and recent search history."
      />

      <section className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="panel-card">
          <h2 className="text-lg font-semibold text-ink">Saved products</h2>
          <p className="mt-2 text-sm text-slate-600">
            Saved products create soft category and brand preference boosts in ranking.
          </p>

          <div className="mt-5 space-y-3">
            {savedLoading ? <p className="text-sm text-slate-500">Loading saved products...</p> : null}
            {!savedLoading && savedProducts.length === 0 ? (
              <p className="text-sm text-slate-500">
                No saved products yet. Save items from the catalog to start personalizing ranking.
              </p>
            ) : null}
            {savedProducts.map((product) => (
              <article key={product.id} className="rounded-3xl border border-slate-200 px-4 py-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">
                      {product.categoryName}
                    </p>
                    <h3 className="mt-1 text-base font-semibold text-ink">{product.name}</h3>
                    <p className="mt-1 text-sm text-slate-500">{product.brandName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Price</p>
                    <p className="text-base font-semibold text-ink">
                      {product.currencyCode} {product.price}
                    </p>
                  </div>
                </div>
                <div className="mt-4 flex items-center justify-between gap-3">
                  <div className="flex flex-wrap gap-2">
                    <span className="rounded-full bg-mist px-3 py-1 text-xs font-medium text-pine">
                      Protein {product.proteinGrams ?? "N/A"}g
                    </span>
                    <span className="rounded-full bg-sand px-3 py-1 text-xs font-medium text-ember">
                      Sugar {product.sugarGrams ?? "N/A"}g
                    </span>
                  </div>
                  <Link
                    to={`/products/${product.productId}`}
                    className="rounded-full bg-ink px-4 py-2 text-sm font-medium text-white no-underline transition hover:bg-pine"
                  >
                    View product
                  </Link>
                </div>
              </article>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <div className="panel-card">
            <h2 className="text-lg font-semibold text-ink">Recent searches</h2>
            <p className="mt-2 text-sm text-slate-600">
              Recent query topics are used as soft ranking boosts rather than hard filters.
            </p>

            <div className="mt-5 space-y-3">
              {historyLoading ? <p className="text-sm text-slate-500">Loading search history...</p> : null}
              {!historyLoading && searchHistory.length === 0 ? (
                <p className="text-sm text-slate-500">No advisor or search history recorded yet.</p>
              ) : null}
              {searchHistory.map((entry) => (
                <div key={entry.id} className="rounded-3xl border border-slate-200 px-4 py-3">
                  <p className="text-sm font-medium text-ink">{entry.queryText}</p>
                  <p className="mt-1 text-xs uppercase tracking-[0.16em] text-slate-500">
                    {entry.categoryHint || "General"} {entry.goal ? `• ${entry.goal}` : ""}
                  </p>
                </div>
              ))}
            </div>
          </div>

          <PlaceholderPanel
            title="Why this matters"
            description="Lightweight personalization should influence ranking, not override the current query."
            bullets={[
              "Saved brands and categories create soft preference boosts.",
              "Recent search topics improve ranking relevance for recurring interests.",
              "Current query intent still outranks old user history.",
            ]}
          />
        </div>
      </section>
    </div>
  );
}
