import { apiClient } from "./http";

export async function fetchUserProfile(userReference) {
  const response = await apiClient.get("/api/v1/personalization/profile", {
    params: { userReference },
  });
  return response.data.data;
}

export async function updateUserProfile(payload) {
  const response = await apiClient.put("/api/v1/personalization/profile", payload);
  return response.data.data;
}

export async function fetchSavedProducts(userReference) {
  const response = await apiClient.get("/api/v1/personalization/saved-products", {
    params: { userReference },
  });
  return response.data.data;
}

export async function saveProduct(payload) {
  const response = await apiClient.post("/api/v1/personalization/saved-products", payload);
  return response.data.data;
}

export async function removeSavedProduct(userReference, productId) {
  const response = await apiClient.delete(`/api/v1/personalization/saved-products/${productId}`, {
    params: { userReference },
  });
  return response.data.data;
}

export async function fetchSearchHistory(userReference) {
  const response = await apiClient.get("/api/v1/personalization/search-history", {
    params: { userReference },
  });
  return response.data.data;
}

export async function recordSearchHistory(payload) {
  const response = await apiClient.post("/api/v1/personalization/search-history", payload);
  return response.data.data;
}
