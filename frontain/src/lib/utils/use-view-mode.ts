"use client";

import { useLayoutEffect } from "react";
import { useFilesStore } from "@/store/files-store";
import type { ViewMode } from "@/types/file.types";

/**
 * Returns the effective view mode from persisted/store state.
 *
 * The inline script in layout.tsx sets `data-view-mode` on &lt;html&gt; before
 * the first paint, so the correct CSS-toggled skeleton shows immediately.
 * This hook only syncs the Zustand store from localStorage.
 */
export function useEffectiveViewMode(): ViewMode {
  const storeMode = useFilesStore((s) => s.viewMode);

  // Sync Zustand store from localStorage before paint.
  // Only writes to the store (not DOM/localStorage — the inline script
  // in layout.tsx already set the data-view-mode attribute).
  useLayoutEffect(() => {
    const saved = localStorage.getItem("viewMode");
    if (saved === "grid" || saved === "list") {
      useFilesStore.setState({ viewMode: saved });
    }
  }, []);

  return storeMode;
}
