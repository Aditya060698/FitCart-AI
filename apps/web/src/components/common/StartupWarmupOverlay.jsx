import { useEffect, useMemo, useRef, useState } from "react";

import { env } from "../../lib/config/env";

const STARTUP_WAIT_SECONDS = 50;
const WARMUP_STORAGE_KEY = "fitcart-startup-warmup-complete";
const WARMUP_TIMEOUT_MS = 60_000;

function buildWarmupUrl(baseUrl, path) {
  return new URL(path, baseUrl).toString();
}

async function warmService(url) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), WARMUP_TIMEOUT_MS);

  try {
    await fetch(url, {
      method: "GET",
      cache: "no-store",
      signal: controller.signal,
    });
  } catch {
    // Warmup is best-effort. The countdown remains the user-facing fallback.
  } finally {
    window.clearTimeout(timeoutId);
  }
}

function shouldShowWarmup() {
  try {
    return sessionStorage.getItem(WARMUP_STORAGE_KEY) !== "true";
  } catch {
    return true;
  }
}

function markWarmupComplete() {
  try {
    sessionStorage.setItem(WARMUP_STORAGE_KEY, "true");
  } catch {
    // Ignore storage failures and continue with the current page view only.
  }
}

export function StartupWarmupOverlay() {
  const [isVisible, setIsVisible] = useState(() => shouldShowWarmup());
  const [secondsRemaining, setSecondsRemaining] = useState(STARTUP_WAIT_SECONDS);
  const hasStartedRef = useRef(false);

  const warmupTargets = useMemo(
    () => [
      buildWarmupUrl(env.apiBaseUrl, "/api/v1/health"),
      buildWarmupUrl(env.aiBaseUrl, "/health"),
    ],
    [],
  );

  useEffect(() => {
    if (!isVisible || hasStartedRef.current) {
      return undefined;
    }

    hasStartedRef.current = true;

    void Promise.allSettled(warmupTargets.map((target) => warmService(target)));

    const intervalId = window.setInterval(() => {
      setSecondsRemaining((current) => (current > 1 ? current - 1 : 0));
    }, 1000);

    const timeoutId = window.setTimeout(() => {
      markWarmupComplete();
      setIsVisible(false);
    }, STARTUP_WAIT_SECONDS * 1000);

    return () => {
      window.clearInterval(intervalId);
      window.clearTimeout(timeoutId);
    };
  }, [isVisible, warmupTargets]);

  if (!isVisible) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/55 px-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-[28px] border border-white/20 bg-slate-950/90 p-6 text-white shadow-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.3em] text-emerald-300">Starting services</p>
        <h2 className="mt-3 text-2xl font-semibold tracking-tight">FitCart is waking up the backend</h2>
        <p className="mt-3 text-sm leading-7 text-slate-200">
          We are sending startup requests to the API and AI services. Please wait while Render brings them online.
        </p>
        <div className="mt-6 rounded-2xl border border-white/10 bg-white/5 p-4">
          <p className="text-sm text-slate-300">Estimated wait time</p>
          <p className="mt-2 text-4xl font-semibold tabular-nums text-white">{secondsRemaining}s</p>
        </div>
      </div>
    </div>
  );
}
