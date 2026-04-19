import { useQuery } from "@tanstack/react-query";

import { fetchProducts } from "../../../lib/api/products";

export function useProductsQuery(params) {
  return useQuery({
    queryKey: ["products", params],
    queryFn: () => fetchProducts(params),
  });
}
