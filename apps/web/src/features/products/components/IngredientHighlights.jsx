export function IngredientHighlights({ product }) {
  const highlights = deriveIngredientHighlights(product);

  return (
    <section className="panel-card">
      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">Highlights</p>
      <h2 className="mt-2 text-xl font-semibold text-ink">Ingredient and usage cues</h2>
      <div className="mt-5 grid gap-3 sm:grid-cols-2">
        {highlights.map((highlight) => (
          <article key={highlight.title} className="rounded-3xl bg-mist px-4 py-4">
            <h3 className="text-sm font-semibold text-ink">{highlight.title}</h3>
            <p className="mt-2 text-sm leading-7 text-slate-600">{highlight.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

function deriveIngredientHighlights(product) {
  const text = `${product.name} ${product.description || ""} ${product.shortDescription || ""}`.toLowerCase();
  const items = [];

  if (text.includes("whey")) {
    items.push({
      title: "Whey-focused protein source",
      description: "Position this as a fast-scan signal for users looking for familiar muscle-gain and recovery products.",
    });
  }

  if (text.includes("creatine")) {
    items.push({
      title: "Creatine support cue",
      description: "This product likely belongs in strength and performance comparison sets.",
    });
  }

  if (text.includes("plant")) {
    items.push({
      title: "Plant-based profile",
      description: "Highlight this for diet-sensitive users and comparison flows that screen for non-dairy options.",
    });
  }

  items.push({
    title: "Category-led interpretation",
    description: `Current backend detail is strongest on exact catalog facts. As ingredient endpoints land, this panel should evolve into grounded nutrient and formulation highlights for ${product.category?.name || "this category"}.`,
  });

  return items.slice(0, 4);
}
