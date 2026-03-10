"use client";

import { useEffect, useState } from "react";
import { useFilesStore } from "@/store/files-store";
import type { ViewMode } from "@/types/file.types";

/**
 * Returns the effective view mode for the current screen size.
 * On mobile (< 640px) it always returns "grid" regardless of the store value,
 * because the list toggle is hidden on small screens.
 */
export function useEffectiveViewMode(): ViewMode {
  const viewMode = useFilesStore((s) => s.viewMode);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const mq = window.matchMedia("(max-width: 639px)");
    setIsMobile(mq.matches);
    const handler = (e: MediaQueryListEvent) => setIsMobile(e.matches);
    mq.addEventListener("change", handler);
    return () => mq.removeEventListener("change", handler);
  }, []);

  return isMobile ? "grid" : viewMode;
}
