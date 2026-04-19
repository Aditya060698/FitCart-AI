export function ReviewSnapshot({ reviews = [] }) {
  const average =
    reviews.length > 0
      ? (reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length).toFixed(1)
      : null;

  return (
    <section className="panel-card">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">Reviews</p>
          <h2 className="mt-2 text-xl font-semibold text-ink">Review snapshot</h2>
        </div>
        <div className="rounded-3xl bg-sand px-4 py-3 text-right">
          <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Recent signal</p>
          <p className="mt-1 text-lg font-semibold text-ink">
            {average ? `${average}/5 avg` : "No ratings yet"}
          </p>
        </div>
      </div>

      {reviews.length === 0 ? (
        <p className="mt-5 text-sm leading-7 text-slate-600">
          No recent reviews were returned by the backend. This section is ready for AI review summaries once that service is connected.
        </p>
      ) : (
        <div className="mt-5 space-y-3">
          {reviews.slice(0, 3).map((review) => (
            <article key={review.id} className="rounded-3xl border border-slate-200 bg-white px-4 py-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="text-sm font-semibold text-ink">{review.reviewTitle || "Untitled review"}</h3>
                  <p className="mt-1 text-xs uppercase tracking-[0.14em] text-slate-500">
                    {review.reviewerName || "Anonymous"} • {review.rating}/5
                  </p>
                </div>
                {review.verifiedPurchase ? (
                  <span className="rounded-full bg-mist px-3 py-1 text-xs font-medium text-pine">
                    Verified
                  </span>
                ) : null}
              </div>
              <p className="mt-3 text-sm leading-7 text-slate-600">{review.reviewBody}</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
