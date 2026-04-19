import { useEffect, useMemo } from "react";
import { useSearchParams } from "react-router-dom";

import { PageHeader } from "../components/common/PageHeader";
import { ComparisonTable } from "../features/compare/components/ComparisonTable";
import { useComparison } from "../features/compare/context/ComparisonContext";
import { useComparisonProductsQuery } from "../features/compare/hooks/useComparisonProductsQuery";

export function ComparePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { productIds, clearProducts, addProduct } = useComparison();

  const routeIds = useMemo(() => {
    const raw = searchParams.get("ids");
    if (!raw) {
      return productIds;
    }

    return raw
      .split(",")
      .map((value) => Number(value.trim()))
      .filter((value) => Number.isInteger(value) && value > 0)
      .slice(0, 4);
  }, [productIds, searchParams]);

  useEffect(() => {
    routeIds.forEach((id) => addProduct(id));
  }, [addProduct, routeIds]);

  useEffect(() => {
    if (productIds.length > 0) {
      setSearchParams({ ids: productIds.join(",") }, { replace: true });
      return;
    }

    setSearchParams({}, { replace: true });
  }, [productIds, setSearchParams]);

  const { data: products, isLoading, isError, failedCount, successfulCount } = useComparisonProductsQuery(routeIds);

  return (
    <div className="space-y-6">
      <section className="page-shell">
        <PageHeader
          eyebrow="Comparison"
          title="Compare 2 to 4 products side by side"
          description="A strong comparison UX makes differences easy to scan. Exact attributes should be shown in a structured table, while summaries and highlights should help users understand trade-offs quickly."
          actions={
            <button
              type="button"
              onClick={clearProducts}
              className="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-ink hover:text-ink"
            >
              Clear selection
            </button>
          }
        />

        <div className="mt-6 rounded-3xl border border-slate-200 bg-sand/60 px-5 py-4 text-sm leading-7 text-slate-700">
          Current compare set: <span className="font-semibold text-ink">{routeIds.length}</span> selected.
          The strongest comparison flows surface exact differences first, then interpretation second.
        </div>

        <div className="mt-8">
          {routeIds.length < 2 ? (
            <div className="rounded-[28px] border border-dashed border-slate-300 bg-white px-6 py-10 text-center">
              <p className="text-lg font-semibold text-ink">Add at least 2 products to compare</p>
              <p className="mt-3 text-sm leading-7 text-slate-600">
                Use the “Add to compare” actions on product cards or product detail pages. You can compare up to 4 products in one view.
              </p>
            </div>
          ) : isLoading ? (
            <div className="h-72 animate-pulse rounded-[28px] bg-white/70" />
          ) : isError && successfulCount < 2 ? (
            <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center">
              <p className="text-lg font-semibold text-ink">Comparison data unavailable</p>
              <p className="mt-3 text-sm leading-7 text-slate-600">
                Too many selected products failed to load from the backend, so a meaningful comparison cannot be shown yet.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {failedCount > 0 ? (
                <div className="rounded-3xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm leading-7 text-amber-900">
                  {failedCount} selected product{failedCount > 1 ? "s" : ""} could not be loaded. Showing comparison for the
                  available products instead.
                </div>
              ) : null}
              <ComparisonTable products={products} />
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
