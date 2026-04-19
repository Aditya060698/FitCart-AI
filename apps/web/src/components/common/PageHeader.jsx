export function PageHeader({ eyebrow, title, description, actions }) {
  return (
    <div className="flex flex-col gap-4 border-b border-slate-200/80 pb-6 md:flex-row md:items-end md:justify-between">
      <div>
        {eyebrow ? (
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">{eyebrow}</p>
        ) : null}
        <h1 className="page-heading">{title}</h1>
        {description ? <p className="page-subheading">{description}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap gap-3">{actions}</div> : null}
    </div>
  );
}
