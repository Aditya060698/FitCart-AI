const ROWS = [
  { label: "Brand", accessor: (product) => product.brand?.name || "N/A" },
  { label: "Category", accessor: (product) => product.category?.name || "N/A" },
  { label: "Price", accessor: (product) => `${product.currencyCode} ${product.price}` },
  { label: "Status", accessor: (product) => (product.active ? "Active" : "Inactive") },
  { label: "Highlights", accessor: (product) => deriveHighlights(product).join(", ") || "N/A" },
  { label: "Review snapshot", accessor: (product) => reviewSummary(product) },
  { label: "Description", accessor: (product) => product.shortDescription || product.description || "N/A" },
];

export function ComparisonTable({ products }) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full border-collapse">
          <thead>
            <tr className="bg-sand/70">
              <th className="border-b border-slate-200 px-4 py-4 text-left text-sm font-semibold text-ink">
                Attribute
              </th>
              {products.map((product) => (
                <th
                  key={product.id}
                  className="border-b border-l border-slate-200 px-4 py-4 text-left align-top text-sm font-semibold text-ink"
                >
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.16em] text-pine">
                      {product.category?.name}
                    </p>
                    <p className="mt-2 text-base font-semibold text-ink">{product.name}</p>
                    <p className="mt-1 text-sm font-normal text-slate-500">{product.brand?.name}</p>
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {ROWS.map((row) => (
              <tr key={row.label} className="align-top">
                <td className="border-b border-slate-200 bg-slate-50 px-4 py-4 text-sm font-medium text-slate-700">
                  {row.label}
                </td>
                {products.map((product) => (
                  <td
                    key={`${product.id}-${row.label}`}
                    className="border-b border-l border-slate-200 px-4 py-4 text-sm leading-7 text-slate-600"
                  >
                    {row.accessor(product)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function deriveHighlights(product) {
  const highlights = [];
  const text = `${product.name} ${product.description || ""} ${product.shortDescription || ""}`.toLowerCase();

  if (text.includes("whey")) highlights.push("Whey protein");
  if (text.includes("creatine")) highlights.push("Creatine support");
  if (text.includes("plant")) highlights.push("Plant-based");
  if (text.includes("recovery")) highlights.push("Recovery oriented");

  if (highlights.length === 0 && product.category?.name) {
    highlights.push(`${product.category.name} focused`);
  }

  return highlights.slice(0, 3);
}

function reviewSummary(product) {
  const count = product.reviews?.length || 0;

  if (count === 0) {
    return "No recent reviews returned";
  }

  const avg =
    product.reviews.reduce((sum, review) => sum + review.rating, 0) / count;

  return `${count} reviews, avg ${avg.toFixed(1)}/5`;
}
