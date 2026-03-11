"use client";

import { useEffect, useState } from "react";
import { useFilesStore } from "@/store/files-store";
import type { ViewMode } from "@/types/file.types";

const MOBILE_BREAKPOINT = 768;

/**
 * Returns the effective view mode. Forces "grid" on mobile screens.
 */
export function useEffectiveViewMode(): ViewMode {
  const storeMode = useFilesStore((s) => s.viewMode);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const check = () => setIsMobile(window.innerWidth < MOBILE_BREAKPOINT);
    check();
    window.addEventListener("resize", check);
    return () => window.removeEventListener("resize", check);
  }, []);

  return isMobile ? "grid" : storeMode;
}
