import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { AddToCompareButton } from "../../compare/components/AddToCompareButton";
import { fetchSavedProducts } from "../../../lib/api/personalization";
import { demoUserReference } from "../../../lib/personalization/user";
import { SaveProductButton } from "./SaveProductButton";

export function ProductCard({ product }) {
  const { data: savedProducts = [] } = useQuery({
    queryKey: ["saved-products", demoUserReference],
    queryFn: () => fetchSavedProducts(demoUserReference),
  });

  const isSaved = savedProducts.some((savedProduct) => savedProduct.productId === product.id);

  return (
    <article className="panel-card flex h-full flex-col transition hover:-translate-y-0.5 hover:shadow-panel">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">
            {product.categoryName || "Category"}
          </p>
          <h3 className="mt-2 text-lg font-semibold text-ink">{product.name}</h3>
          <p className="mt-1 text-sm text-slate-500">{product.brandName || "Brand"}</p>
        </div>
        <div className="rounded-2xl bg-mist px-3 py-2 text-right">
          <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Price</p>
          <p className="text-base font-semibold text-ink">
            {product.currencyCode || "USD"} {product.price}
          </p>
        </div>
      </div>

      <p className="mt-4 flex-1 text-sm leading-7 text-slate-600">
        {product.shortDescription || "Product description will appear here once wired to the backend."}
      </p>

      <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          <span className="rounded-full bg-sand px-3 py-1 text-xs font-medium text-ember">
            {product.slug}
          </span>
          <span className="rounded-full bg-mist px-3 py-1 text-xs font-medium text-pine">
            {product.brandName}
          </span>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <SaveProductButton productId={product.id} isSaved={isSaved} />
          <AddToCompareButton productId={product.id} />
          <Link
            to={`/products/${product.id}`}
            className="rounded-full bg-ink px-4 py-2 text-sm font-medium text-white no-underline transition hover:bg-pine"
          >
            View details
          </Link>
        </div>
      </div>
    </article>
  );
}
