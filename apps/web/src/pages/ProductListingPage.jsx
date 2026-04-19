import { useMemo } from "react";
import { useSearchParams } from "react-router-dom";

import { PageHeader } from "../components/common/PageHeader";
import { PlaceholderPanel } from "../components/common/PlaceholderPanel";
import { CatalogToolbar } from "../features/products/components/CatalogToolbar";
import { FilterSidebar } from "../features/products/components/FilterSidebar";
import { PaginationControls } from "../features/products/components/PaginationControls";
import { ProductCard } from "../features/products/components/ProductCard";
import { useCatalogFiltersQuery } from "../features/products/hooks/useCatalogFiltersQuery";
import { useProductsQuery } from "../features/products/hooks/useProductsQuery";
import {
  buildCatalogSearchParams,
  parseCatalogSearchParams,
  toProductQuery,
} from "../features/products/utils/catalogQueryParams";

export function ProductListingPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = useMemo(() => parseCatalogSearchParams(searchParams), [searchParams]);
  const productQuery = useMemo(() => toProductQuery(filters), [filters]);

  const { data, isLoading, isError } = useProductsQuery(productQuery);
  const { categoriesQuery, brandsQuery } = useCatalogFiltersQuery();

  const products = data?.content || [];
  const categories = categoriesQuery.data || [];
  const brands = brandsQuery.data || [];

  function updateFilters(partial) {
    const nextFilters = {
      ...filters,
      ...partial,
      page: partial.page ?? 0,
    };
    setSearchParams(buildCatalogSearchParams(nextFilters));
  }

  function handleSortChange(sortBy, sortDir) {
    updateFilters({ sortBy, sortDir });
  }

  function handlePageChange(nextPage) {
    updateFilters({ page: nextPage });
  }

  function resetFilters() {
    setSearchParams(buildCatalogSearchParams({ ...filters, search: "", categoryId: "", brandId: "", active: "true", sortBy: "newest", sortDir: "desc", page: 0, size: filters.size }));
  }

  return (
    <div className="space-y-6">
      <section className="page-shell">
        <PageHeader
          eyebrow="Catalog"
          title="Product catalog built around server-driven listing patterns"
          description="The catalog UI uses controlled inputs, query-param-synced filter state, backend pagination, and a route structure that scales to real search and merchandising workflows."
        />

        <div className="mt-8">
          <CatalogToolbar
            totalProducts={data?.totalElements || 0}
            searchValue={filters.search}
            onSearchChange={(value) => updateFilters({ search: value })}
            sortBy={filters.sortBy}
            sortDir={filters.sortDir}
            onSortChange={handleSortChange}
          />
        </div>

        <div className="mt-6 grid gap-6 lg:grid-cols-[280px_minmax(0,1fr)]">
          <FilterSidebar
            filters={filters}
            categories={categories}
            brands={brands}
            onFilterChange={(field, value) => updateFilters({ [field]: value })}
            onReset={resetFilters}
          />

          <div>
            {isLoading ? (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {Array.from({ length: 6 }).map((_, index) => (
                  <div key={index} className="panel-card h-56 animate-pulse bg-slate-100" />
                ))}
              </div>
            ) : isError ? (
              <PlaceholderPanel
                title="Product listing unavailable"
                description="The backend product API could not be reached or returned an error. Once the API is running, this page will populate automatically."
              />
            ) : products.length === 0 ? (
              <PlaceholderPanel
                title="No products matched these filters"
                description="This is the expected empty-state behavior for a catalog UI. Keep filters URL-driven so users can share or revisit exact result states."
              />
            ) : (
              <>
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {products.map((product) => (
                    <ProductCard key={product.id} product={product} />
                  ))}
                </div>
                <PaginationControls
                  page={data.page}
                  totalPages={data.totalPages}
                  totalElements={data.totalElements}
                  onPageChange={handlePageChange}
                />
              </>
            )}
          </div>
        </div>

        {categoriesQuery.isError || brandsQuery.isError ? (
          <div className="mt-6">
            <PlaceholderPanel
              title="Filter metadata unavailable"
              description="The product list can still load without brand and category lookup data, but the sidebar options depend on those supporting endpoints."
            />
          </div>
        ) : null}
      </section>
    </div>
  );
}
