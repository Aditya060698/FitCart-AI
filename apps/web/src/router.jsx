import { createBrowserRouter } from "react-router-dom";

import { AppLayout } from "./layouts/AppLayout";
import { AiAdvisorPage } from "./pages/AiAdvisorPage";
import { ComparePage } from "./pages/ComparePage";
import { HomePage } from "./pages/HomePage";
import { ProductDetailsPage } from "./pages/ProductDetailsPage";
import { ProductListingPage } from "./pages/ProductListingPage";
import { SavedProductsPage } from "./pages/SavedProductsPage";
import { UploadDocumentPage } from "./pages/UploadDocumentPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "products", element: <ProductListingPage /> },
      { path: "products/:productId", element: <ProductDetailsPage /> },
      { path: "compare", element: <ComparePage /> },
      { path: "advisor", element: <AiAdvisorPage /> },
      { path: "upload", element: <UploadDocumentPage /> },
      { path: "saved", element: <SavedProductsPage /> },
    ],
  },
]);
