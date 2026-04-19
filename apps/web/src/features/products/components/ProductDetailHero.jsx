import { Link } from "react-router-dom";

import { AddToCompareButton } from "../../compare/components/AddToCompareButton";

export function ProductDetailHero({ product }) {
  return (
    <section className="page-shell">
      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">
            {product.category?.name || "Product"}
          </p>
          <h1 className="mt-3 text-3xl font-semibold tracking-tight text-ink md:text-5xl">
            {product.name}
          </h1>
          <p className="mt-4 max-w-3xl text-sm leading-8 text-slate-600 md:text-base">
            {product.description ||
              "Detailed product explanation will appear here as catalog depth, nutrient data, and AI summaries expand."}
          </p>

          <div className="mt-6 flex flex-wrap gap-3">
            <span className="rounded-full bg-mist px-4 py-2 text-sm font-medium text-pine">
              {product.brand?.name || "Unknown brand"}
            </span>
            <span className="rounded-full bg-sand px-4 py-2 text-sm font-medium text-ember">
              {product.slug}
            </span>
            <span className="rounded-full bg-slate-100 px-4 py-2 text-sm font-medium text-slate-700">
              {product.active ? "Active listing" : "Inactive listing"}
            </span>
          </div>
        </div>

        <div className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">Quick summary</p>
          <div className="mt-5 space-y-4">
            <div>
              <p className="text-sm text-slate-500">Price</p>
              <p className="mt-1 text-3xl font-semibold tracking-tight text-ink">
                {product.currencyCode} {product.price}
              </p>
            </div>
            <div>
              <p className="text-sm text-slate-500">Short description</p>
              <p className="mt-1 text-sm leading-7 text-slate-600">
                {product.shortDescription || "No short description available."}
              </p>
            </div>
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <AddToCompareButton productId={product.id} variant="solid" />
            <Link
              to="/compare"
              className="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 no-underline transition hover:border-ink hover:text-ink"
            >
              Open compare
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
