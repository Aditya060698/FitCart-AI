const DEFAULT_ROWS = [
  { label: "Brand", accessor: (product) => product.brand?.name || "N/A" },
  { label: "Category", accessor: (product) => product.category?.name || "N/A" },
  { label: "Price", accessor: (product) => `${product.currencyCode} ${product.price}` },
  { label: "Availability", accessor: (product) => (product.active ? "Active" : "Inactive") },
  { label: "SKU", accessor: (product) => product.sku || "N/A" },
  { label: "Slug", accessor: (product) => product.slug || "N/A" },
];

export function ProductSpecTable({ product }) {
  return (
    <section className="panel-card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">Specifications</p>
          <h2 className="mt-2 text-xl font-semibold text-ink">Nutrition and product details</h2>
        </div>
      </div>

      <div className="mt-5 overflow-hidden rounded-3xl border border-slate-200">
        <table className="min-w-full border-collapse">
          <tbody>
            {DEFAULT_ROWS.map((row) => (
              <tr key={row.label}>
                <td className="w-1/3 border-b border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700">
                  {row.label}
                </td>
                <td className="border-b border-slate-200 px-4 py-3 text-sm text-slate-600">
                  {row.accessor(product)}
                </td>
              </tr>
            ))}
            <tr>
              <td className="w-1/3 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700">
                Nutrition note
              </td>
              <td className="px-4 py-3 text-sm text-slate-600">
                The backend currently returns core product metadata. This table is structured so detailed nutrient rows can be added when nutrient endpoints are exposed.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  );
}
