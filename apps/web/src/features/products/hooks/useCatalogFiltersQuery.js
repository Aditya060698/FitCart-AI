import { useQuery } from "@tanstack/react-query";

import { fetchBrands, fetchCategories } from "../../../lib/api/products";

export function useCatalogFiltersQuery() {
  const categoriesQuery = useQuery({
    queryKey: ["catalog-categories"],
    queryFn: fetchCategories,
    staleTime: 5 * 60 * 1000,
  });

  const brandsQuery = useQuery({
    queryKey: ["catalog-brands"],
    queryFn: fetchBrands,
    staleTime: 5 * 60 * 1000,
  });

  return {
    categoriesQuery,
    brandsQuery,
  };
}
