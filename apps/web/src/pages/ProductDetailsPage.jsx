import { useParams } from "react-router-dom";

import { PageHeader } from "../components/common/PageHeader";
import { IngredientHighlights } from "../features/products/components/IngredientHighlights";
import { ProductDetailHero } from "../features/products/components/ProductDetailHero";
import { ProductSpecTable } from "../features/products/components/ProductSpecTable";
import { ReviewSnapshot } from "../features/products/components/ReviewSnapshot";
import { useProductDetailQuery } from "../features/products/hooks/useProductDetailQuery";

export function ProductDetailsPage() {
  const { productId } = useParams();
  const { data, isLoading, isError } = useProductDetailQuery(productId);

  if (isLoading) {
    return <div className="page-shell h-72 animate-pulse bg-white/60" />;
  }

  if (isError || !data) {
    return (
      <div className="page-shell">
        <PageHeader
          eyebrow="Product Detail"
          title="Product unavailable"
          description="The product detail route is set up, but this item could not be fetched from the backend."
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <ProductDetailHero product={data} />

      <section className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">
        <ProductSpecTable product={data} />
        <IngredientHighlights product={data} />
      </section>

      <ReviewSnapshot reviews={data.reviews || []} />
    </div>
  );
}
