import { createContext, useContext, useEffect, useMemo, useState } from "react";

const MAX_COMPARE_ITEMS = 4;
const STORAGE_KEY = "fitcart-compare-products";

const ComparisonContext = createContext(null);

export function ComparisonProvider({ children }) {
  const [productIds, setProductIds] = useState(() => {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(productIds));
  }, [productIds]);

  function addProduct(productId) {
    setProductIds((current) => {
      if (current.includes(productId) || current.length >= MAX_COMPARE_ITEMS) {
        return current;
      }

      return [...current, productId];
    });
  }

  function removeProduct(productId) {
    setProductIds((current) => current.filter((id) => id !== productId));
  }

  function toggleProduct(productId) {
    setProductIds((current) => {
      if (current.includes(productId)) {
        return current.filter((id) => id !== productId);
      }

      if (current.length >= MAX_COMPARE_ITEMS) {
        return current;
      }

      return [...current, productId];
    });
  }

  function clearProducts() {
    setProductIds([]);
  }

  const value = useMemo(
    () => ({
      productIds,
      maxItems: MAX_COMPARE_ITEMS,
      addProduct,
      removeProduct,
      toggleProduct,
      clearProducts,
      isSelected: (productId) => productIds.includes(productId),
      canAddMore: productIds.length < MAX_COMPARE_ITEMS,
    }),
    [productIds],
  );

  return <ComparisonContext.Provider value={value}>{children}</ComparisonContext.Provider>;
}

export function useComparison() {
  const context = useContext(ComparisonContext);

  if (!context) {
    throw new Error("useComparison must be used within a ComparisonProvider");
  }

  return context;
}
