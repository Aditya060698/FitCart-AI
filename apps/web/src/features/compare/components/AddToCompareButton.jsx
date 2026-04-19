import { useComparison } from "../context/ComparisonContext";

export function AddToCompareButton({ productId, variant = "ghost" }) {
  const { isSelected, toggleProduct, canAddMore, maxItems } = useComparison();
  const selected = isSelected(productId);
  const disabled = !selected && !canAddMore;

  const className =
    variant === "solid"
      ? [
          "rounded-full px-4 py-2 text-sm font-medium transition",
          selected ? "bg-pine text-white" : "bg-ink text-white hover:bg-pine",
          disabled ? "cursor-not-allowed opacity-50" : "",
        ].join(" ")
      : [
          "rounded-full border px-4 py-2 text-sm font-medium transition",
          selected
            ? "border-pine bg-mist text-pine"
            : "border-slate-300 bg-white text-slate-700 hover:border-ink hover:text-ink",
          disabled ? "cursor-not-allowed opacity-50" : "",
        ].join(" ");

  return (
    <button
      type="button"
      disabled={disabled}
      onClick={() => toggleProduct(productId)}
      className={className}
      title={disabled ? `You can compare up to ${maxItems} products` : undefined}
    >
      {selected ? "Added to compare" : "Add to compare"}
    </button>
  );
}
