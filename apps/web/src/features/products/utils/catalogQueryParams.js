const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 9;
const DEFAULT_SORT_BY = "newest";
const DEFAULT_SORT_DIR = "desc";

export function parseCatalogSearchParams(searchParams) {
  return {
    search: searchParams.get("search") || "",
    categoryId: searchParams.get("categoryId") || "",
    brandId: searchParams.get("brandId") || "",
    active: searchParams.get("active") || "true",
    sortBy: searchParams.get("sortBy") || DEFAULT_SORT_BY,
    sortDir: searchParams.get("sortDir") || DEFAULT_SORT_DIR,
    page: Number(searchParams.get("page") || DEFAULT_PAGE),
    size: Number(searchParams.get("size") || DEFAULT_SIZE),
  };
}

export function buildCatalogSearchParams(filters) {
  const params = new URLSearchParams();

  if (filters.search) {
    params.set("search", filters.search);
  }
  if (filters.categoryId) {
    params.set("categoryId", filters.categoryId);
  }
  if (filters.brandId) {
    params.set("brandId", filters.brandId);
  }
  if (filters.active) {
    params.set("active", filters.active);
  }
  if (filters.sortBy && filters.sortBy !== DEFAULT_SORT_BY) {
    params.set("sortBy", filters.sortBy);
  }
  if (filters.sortDir && filters.sortDir !== DEFAULT_SORT_DIR) {
    params.set("sortDir", filters.sortDir);
  }
  if (filters.page && filters.page !== DEFAULT_PAGE) {
    params.set("page", String(filters.page));
  }
  if (filters.size && filters.size !== DEFAULT_SIZE) {
    params.set("size", String(filters.size));
  }

  return params;
}

export function toProductQuery(filters) {
  return {
    page: filters.page,
    size: filters.size,
    search: filters.search || undefined,
    categoryId: filters.categoryId || undefined,
    brandId: filters.brandId || undefined,
    active: filters.active === "" ? undefined : filters.active,
    sortBy: filters.sortBy,
    sortDir: filters.sortDir,
  };
}
