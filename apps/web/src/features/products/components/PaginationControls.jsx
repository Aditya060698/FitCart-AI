export function PaginationControls({
  page,
  totalPages,
  totalElements,
  onPageChange,
}) {
  if (!totalElements) {
    return null;
  }

  return (
    <div className="mt-8 flex flex-col gap-4 rounded-3xl border border-slate-200/80 bg-white px-5 py-4 md:flex-row md:items-center md:justify-between">
      <p className="text-sm text-slate-600">
        Page <span className="font-semibold text-ink">{page + 1}</span> of{" "}
        <span className="font-semibold text-ink">{Math.max(totalPages, 1)}</span>
      </p>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition disabled:cursor-not-allowed disabled:opacity-40 hover:bg-slate-50"
        >
          Previous
        </button>
        <button
          type="button"
          onClick={() => onPageChange(page + 1)}
          disabled={page + 1 >= totalPages}
          className="rounded-full bg-ink px-4 py-2 text-sm font-medium text-white transition disabled:cursor-not-allowed disabled:opacity-40 hover:bg-pine"
        >
          Next
        </button>
      </div>
    </div>
  );
}
