import axios from "axios";

import { env } from "../config/env";

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

export const aiClient = axios.create({
  baseURL: env.aiBaseUrl,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

export function getApiErrorMessage(error, fallbackMessage = "Something went wrong. Please try again.") {
  if (error?.code === "ECONNABORTED") {
    return "The request timed out. Please retry.";
  }

  const apiMessage = error?.response?.data?.message;
  if (apiMessage) {
    return apiMessage;
  }

  const nestedMessage = error?.response?.data?.error?.message;
  if (nestedMessage) {
    return nestedMessage;
  }

  if (error?.message) {
    return error.message;
  }

  return fallbackMessage;
}
