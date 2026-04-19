import { apiClient } from "./http";

export async function fetchAdvisorRecommendation(payload) {
  const response = await apiClient.post("/api/v1/advisor/recommend", payload);
  return response.data.data;
}
