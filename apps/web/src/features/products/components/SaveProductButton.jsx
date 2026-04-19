import { useMutation, useQueryClient } from "@tanstack/react-query";

import { removeSavedProduct, saveProduct } from "../../../lib/api/personalization";
import { demoUserReference } from "../../../lib/personalization/user";

export function SaveProductButton({ productId, isSaved = false }) {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async () => {
      if (isSaved) {
        return removeSavedProduct(demoUserReference, productId);
      }
      return saveProduct({ userReference: demoUserReference, productId });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["saved-products", demoUserReference] });
    },
  });

  return (
    <button
      type="button"
      onClick={() => mutation.mutate()}
      disabled={mutation.isPending}
      className="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-pine hover:text-pine disabled:opacity-60"
    >
      {isSaved ? "Saved" : "Save"}
    </button>
  );
}
