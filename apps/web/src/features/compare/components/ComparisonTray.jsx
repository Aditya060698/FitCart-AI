import { Link } from "react-router-dom";

import { useComparison } from "../context/ComparisonContext";

export function ComparisonTray() {
  const { productIds, clearProducts } = useComparison();

  if (productIds.length === 0) {
    return null;
  }

  return (
    <div className="sticky bottom-4 z-20">
      <div className="mx-auto flex max-w-4xl items-center justify-between gap-4 rounded-[28px] border border-white/80 bg-ink px-5 py-4 text-white shadow-panel">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-leaf">Comparison</p>
          <p className="mt-1 text-sm text-slate-200">
            {productIds.length} product{productIds.length > 1 ? "s" : ""} selected for side-by-side evaluation
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={clearProducts}
            className="rounded-full border border-white/20 px-4 py-2 text-sm font-medium text-white transition hover:bg-white/10"
          >
            Clear
          </button>
          <Link
            to={`/compare?ids=${productIds.join(",")}`}
            className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-ink no-underline transition hover:bg-sand"
          >
            Compare now
          </Link>
        </div>
      </div>
    </div>
  );
}
