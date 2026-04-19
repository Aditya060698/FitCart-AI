import { apiClient } from "./http";

export async function fetchAutocompleteSuggestions({ query, types = [], limit = 8 }) {
  const params = {
    query,
    limit,
  };

  if (types.length > 0) {
    params.types = types;
  }

  const response = await apiClient.get("/api/v1/autocomplete", { params });
  return response.data.data;
}
