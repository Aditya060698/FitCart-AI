import { apiClient } from "./http";

export async function fetchProducts(params = {}) {
  const response = await apiClient.get("/api/v1/products", { params });
  return response.data.data;
}

export async function fetchProductDetail(productId) {
  const response = await apiClient.get(`/api/v1/products/${productId}`);
  return response.data.data;
}

export async function fetchCategories() {
  const response = await apiClient.get("/api/v1/categories");
  return response.data.data;
}

export async function fetchBrands() {
  const response = await apiClient.get("/api/v1/brands");
  return response.data.data;
}
