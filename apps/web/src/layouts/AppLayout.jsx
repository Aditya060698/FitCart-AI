import { Outlet } from "react-router-dom";

import { StartupWarmupOverlay } from "../components/common/StartupWarmupOverlay";
import { ComparisonTray } from "../features/compare/components/ComparisonTray";
import { Navbar } from "../components/navigation/Navbar";

export function AppLayout() {
  return (
    <div className="min-h-screen bg-fitcart-glow">
      <StartupWarmupOverlay />
      <Navbar />
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-4 pb-12 pt-6 md:px-6 lg:px-8">
        <Outlet />
        <ComparisonTray />
      </div>
    </div>
  );
}
