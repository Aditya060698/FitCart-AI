export function FilterSidebar({
  filters,
  categories,
  brands,
  onFilterChange,
  onReset,
}) {
  return (
    <aside className="panel-card h-fit lg:sticky lg:top-28">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">Filters</p>
          <h2 className="mt-2 text-lg font-semibold text-ink">Refine results</h2>
        </div>
        <button
          type="button"
          onClick={onReset}
          className="rounded-full border border-slate-300 px-3 py-2 text-xs font-semibold uppercase tracking-[0.12em] text-slate-600 transition hover:bg-slate-50"
        >
          Reset
        </button>
      </div>

      <div className="mt-6 space-y-5">
        <div>
          <label htmlFor="catalog-category" className="text-sm font-medium text-slate-700">
            Category
          </label>
          <select
            id="catalog-category"
            value={filters.categoryId}
            onChange={(event) => onFilterChange("categoryId", event.target.value)}
            className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
          >
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={String(category.id)}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="catalog-brand" className="text-sm font-medium text-slate-700">
            Brand
          </label>
          <select
            id="catalog-brand"
            value={filters.brandId}
            onChange={(event) => onFilterChange("brandId", event.target.value)}
            className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
          >
            <option value="">All brands</option>
            {brands.map((brand) => (
              <option key={brand.id} value={String(brand.id)}>
                {brand.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="catalog-status" className="text-sm font-medium text-slate-700">
            Availability
          </label>
          <select
            id="catalog-status"
            value={filters.active}
            onChange={(event) => onFilterChange("active", event.target.value)}
            className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
          >
            <option value="true">Active only</option>
            <option value="false">Inactive only</option>
            <option value="">All products</option>
          </select>
        </div>
      </div>
    </aside>
  );
}
