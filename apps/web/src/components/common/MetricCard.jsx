export function MetricCard({ label, value, helper }) {
  return (
    <article className="panel-card">
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className="mt-3 text-3xl font-semibold tracking-tight text-ink">{value}</p>
      {helper ? <p className="mt-2 text-sm text-slate-600">{helper}</p> : null}
    </article>
  );
}
