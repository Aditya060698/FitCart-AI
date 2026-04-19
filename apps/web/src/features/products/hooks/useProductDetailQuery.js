import { useQuery } from "@tanstack/react-query";

import { fetchProductDetail } from "../../../lib/api/products";

export function useProductDetailQuery(productId) {
  return useQuery({
    queryKey: ["product-detail", productId],
    queryFn: () => fetchProductDetail(productId),
    enabled: Boolean(productId),
  });
}
