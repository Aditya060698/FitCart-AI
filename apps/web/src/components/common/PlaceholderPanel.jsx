export function PlaceholderPanel({ title, description, bullets = [] }) {
  return (
    <section className="panel-card">
      <h2 className="text-lg font-semibold text-ink">{title}</h2>
      <p className="mt-2 text-sm leading-7 text-slate-600">{description}</p>
      {bullets.length > 0 ? (
        <ul className="mt-4 space-y-2 text-sm text-slate-700">
          {bullets.map((bullet) => (
            <li key={bullet} className="rounded-2xl bg-mist px-4 py-3">
              {bullet}
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}
