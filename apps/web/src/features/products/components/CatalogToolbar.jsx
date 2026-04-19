export function CatalogToolbar({
  totalProducts,
  searchValue,
  onSearchChange,
  sortBy,
  sortDir,
  onSortChange,
}) {
  return (
    <section className="panel-card">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div className="w-full lg:max-w-2xl">
          <label htmlFor="catalog-search" className="text-sm font-medium text-slate-700">
            Search products
          </label>
          <div className="mt-2 flex flex-col gap-3 md:flex-row">
            <input
              id="catalog-search"
              type="text"
              value={searchValue}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder="Search whey, creatine, hydration, plant protein..."
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
            />
            <div className="min-w-40 rounded-2xl bg-mist px-4 py-3 text-sm text-slate-600">
              {totalProducts} results
            </div>
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <div>
            <label htmlFor="catalog-sort-by" className="text-sm font-medium text-slate-700">
              Sort by
            </label>
            <select
              id="catalog-sort-by"
              value={sortBy}
              onChange={(event) => onSortChange(event.target.value, sortDir)}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
            >
              <option value="newest">Newest</option>
              <option value="price">Price</option>
              <option value="name">Name</option>
            </select>
          </div>
          <div>
            <label htmlFor="catalog-sort-dir" className="text-sm font-medium text-slate-700">
              Direction
            </label>
            <select
              id="catalog-sort-dir"
              value={sortDir}
              onChange={(event) => onSortChange(sortBy, event.target.value)}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-pine focus:bg-white"
            >
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>
        </div>
      </div>
    </section>
  );
}
