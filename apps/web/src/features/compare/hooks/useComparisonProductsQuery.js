import { useQueries } from "@tanstack/react-query";

import { fetchProductDetail } from "../../../lib/api/products";

export function useComparisonProductsQuery(productIds) {
  const results = useQueries({
    queries: productIds.map((productId) => ({
      queryKey: ["compare-product", productId],
      queryFn: () => fetchProductDetail(productId),
      enabled: Boolean(productId),
      staleTime: 60 * 1000,
    })),
  });

  return {
    results,
    data: results.map((result) => result.data).filter(Boolean),
    isLoading: results.some((result) => result.isLoading),
    isError: results.some((result) => result.isError),
    failedCount: results.filter((result) => result.isError).length,
    successfulCount: results.filter((result) => result.data).length,
  };
}
