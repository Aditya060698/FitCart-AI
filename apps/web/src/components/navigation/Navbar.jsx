import { NavLink } from "react-router-dom";

const NAV_ITEMS = [
  { to: "/", label: "Home" },
  { to: "/products", label: "Products" },
  { to: "/compare", label: "Compare" },
  { to: "/advisor", label: "AI Advisor" },
  { to: "/upload", label: "Upload" },
  { to: "/saved", label: "Saved" },
];

export function Navbar() {
  return (
    <header className="sticky top-0 z-30 border-b border-white/70 bg-sand/85 backdrop-blur">
      <div className="mx-auto flex w-full max-w-7xl items-center justify-between gap-4 px-4 py-4 md:px-6 lg:px-8">
        <NavLink to="/" className="flex items-center gap-3 text-ink no-underline">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-pine text-sm font-bold uppercase tracking-[0.16em] text-white">
            FC
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-pine">FitCart AI</p>
            <p className="text-sm text-slate-600">Nutrition shopping assistant</p>
          </div>
        </NavLink>

        <nav className="flex flex-wrap items-center justify-end gap-2">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                [
                  "rounded-full px-4 py-2 text-sm font-medium transition",
                  isActive
                    ? "bg-ink text-white shadow-sm"
                    : "text-slate-600 hover:bg-white hover:text-ink",
                ].join(" ")
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  );
}
