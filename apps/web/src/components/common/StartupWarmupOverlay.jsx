import { useEffect, useMemo, useRef, useState } from "react";

import { env } from "../../lib/config/env";

const STARTUP_WAIT_SECONDS = 50;
const WARMUP_STORAGE_KEY = "fitcart-startup-warmup-complete";
const WARMUP_TIMEOUT_MS = 60_000;
const WARMUP_RETRY_MS = 5_000;

function buildWarmupUrl(baseUrl, path) {
  return new URL(path, baseUrl).toString();
}

async function warmService(url) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), WARMUP_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      method: "GET",
      cache: "no-store",
      signal: controller.signal,
    });

    return response.ok;
  } catch {
    return false;
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
  const [serviceStatuses, setServiceStatuses] = useState({
    api: false,
    ai: false,
  });
  const hasStartedRef = useRef(false);
  const retryIntervalRef = useRef(null);
  const countdownIntervalRef = useRef(null);

  const warmupTargets = useMemo(
    () => ({
      api: buildWarmupUrl(env.apiBaseUrl, "/api/v1/health"),
      ai: buildWarmupUrl(env.aiBaseUrl, "/health"),
    }),
    [],
  );

  useEffect(() => {
    if (!isVisible || hasStartedRef.current) {
      return undefined;
    }

    hasStartedRef.current = true;

    const deadline = Date.now() + STARTUP_WAIT_SECONDS * 1000;

    const updateCountdown = () => {
      const remainingMs = Math.max(deadline - Date.now(), 0);
      setSecondsRemaining(Math.ceil(remainingMs / 1000));
    };

    const attemptWarmup = async () => {
      const [apiReady, aiReady] = await Promise.all([
        warmService(warmupTargets.api),
        warmService(warmupTargets.ai),
      ]);

      setServiceStatuses((current) => ({
        api: current.api || apiReady,
        ai: current.ai || aiReady,
      }));
    };

    updateCountdown();
    void attemptWarmup();

    countdownIntervalRef.current = window.setInterval(updateCountdown, 1000);
    retryIntervalRef.current = window.setInterval(() => {
      void attemptWarmup();
    }, WARMUP_RETRY_MS);

    return () => {
      if (countdownIntervalRef.current) {
        window.clearInterval(countdownIntervalRef.current);
      }

      if (retryIntervalRef.current) {
        window.clearInterval(retryIntervalRef.current);
      }
    };
  }, [isVisible, warmupTargets]);

  useEffect(() => {
    if (!serviceStatuses.api || !serviceStatuses.ai) {
      return;
    }

    markWarmupComplete();
    setIsVisible(false);

    if (countdownIntervalRef.current) {
      window.clearInterval(countdownIntervalRef.current);
    }

    if (retryIntervalRef.current) {
      window.clearInterval(retryIntervalRef.current);
    }
  }, [serviceStatuses]);

  if (!isVisible) {
    return null;
  }

  const isStillStarting = secondsRemaining === 0 && (!serviceStatuses.api || !serviceStatuses.ai);

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/55 px-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-[28px] border border-white/20 bg-slate-950/90 p-6 text-white shadow-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.3em] text-emerald-300">Starting services</p>
        <h2 className="mt-3 text-2xl font-semibold tracking-tight">FitCart is waking up the backend</h2>
        <p className="mt-3 text-sm leading-7 text-slate-200">
          {isStillStarting
            ? "Render is still waking the services up. We are continuing to retry the API and AI startup requests."
            : "We are sending startup requests to the API and AI services. Please wait while Render brings them online."}
        </p>
        <div className="mt-6 rounded-2xl border border-white/10 bg-white/5 p-4">
          <p className="text-sm text-slate-300">Estimated wait time</p>
          <p className="mt-2 text-4xl font-semibold tabular-nums text-white">
            {isStillStarting ? "Still starting..." : `${secondsRemaining}s`}
          </p>
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-300">API</p>
            <p className="mt-2 text-sm text-white">{serviceStatuses.api ? "Ready" : "Starting..."}</p>
          </div>
          <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-300">AI</p>
            <p className="mt-2 text-sm text-white">{serviceStatuses.ai ? "Ready" : "Starting..."}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
