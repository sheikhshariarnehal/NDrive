"use client";

import { useEffect, useMemo } from "react";
import { useAuth } from "@/app/providers/auth-provider";
import { useFilesStore } from "@/store/files-store";
import { useUIStore } from "@/store/ui-store";
import { createClient } from "@/lib/supabase/client";
import { FolderGrid } from "@/components/file-grid/folder-grid";
import { FileCard } from "@/components/file-grid/file-card";
import { FileList } from "@/components/file-list/file-list";
import { SuggestedFiles } from "@/components/suggested-files/suggested-files";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { LayoutGrid, List, Info, ChevronDown } from "lucide-react";
import type { DbFile } from "@/types/file.types";
import { useEffectiveViewMode } from "@/lib/utils/use-view-mode";
import { GridViewSkeleton } from "@/components/skeletons/grid-view-skeleton";
import { ListViewSkeleton } from "@/components/skeletons/list-view-skeleton";

export default function DashboardPage() {
  const { user, guestSessionId } = useAuth();
  const { files, folders, viewMode, setViewMode, isLoading, searchQuery, mergeFiles, dataLoaded } =
    useFilesStore();
  const { openFilePicker } = useUIStore();
  const effectiveViewMode = useEffectiveViewMode();

  // (Supplementary fetch for root files was removed as layout.tsx now handles full background hydration of all files)

  // Filter files and folders for root level (no parent) and search
  const rootFolders = useMemo(() => folders.filter((f) => !f.parent_id), [folders]);
  const rootFiles = useMemo(() => files.filter((f) => !f.folder_id), [files]);

  const filteredFiles = useMemo(() => searchQuery
    ? files.filter((f) =>
        f.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : rootFiles, [searchQuery, files, rootFiles]);

  const filteredFolders = useMemo(() => searchQuery
    ? folders.filter((f) =>
        f.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : rootFolders, [searchQuery, folders, rootFolders]);

  const starredFiles = useMemo(() => files.filter((f) => f.is_starred), [files]);
  
  const recentFiles = useMemo(() => [...files]
    .sort(
      (a, b) =>
        new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
    )
    .slice(0, 10), [files]);

  if (isLoading || !dataLoaded) {
    return (
      <div className="flex flex-col">
        {/* Skeleton sticky header */}
        <div className="flex items-center h-11 sm:h-14 sticky top-0 z-20 bg-surface-white -mx-2.5 px-2.5 sm:-mx-4 sm:px-4 lg:-mx-5 lg:px-5">
          <div className="h-5 sm:h-6 w-24 sm:w-36 rounded bg-[#e8eaed] animate-pulse" />
          <div className="ml-auto flex items-center gap-2">
            <div className="hidden sm:flex items-center gap-1 p-0.5 rounded-full border border-[#dadce0] bg-[#f8f9fa]">
              <div className="h-8 w-8 rounded-full bg-[#e8eaed] animate-pulse" />
              <div className="h-8 w-8 rounded-full bg-[#e8eaed] animate-pulse" />
            </div>
            <div className="h-8 w-8 rounded-full bg-[#e8eaed] animate-pulse" />
          </div>
        </div>
        <div className="skeleton-grid"><GridViewSkeleton /></div>
        <div className="skeleton-list"><ListViewSkeleton /></div>
      </div>
    );
  }

  return (
    <div className="flex flex-col">
      {/* Google Drive-style compact sticky header */}
      <div className="flex items-center h-11 sm:h-14 sticky top-0 z-20 bg-surface-white -mx-2.5 px-2.5 sm:-mx-4 sm:px-4 lg:-mx-5 lg:px-5">
        <h1 className="text-[17px] sm:text-[22px] font-normal text-[#202124] flex-1 min-w-0 truncate flex items-center gap-1.5">My Drive <ChevronDown className="h-4 w-4 text-[#5f6368]" /></h1>
        <TooltipProvider>
          <div className="flex items-center gap-2 flex-shrink-0">
            <div className="hidden sm:flex items-center gap-0.5 p-0.5 rounded-full border border-[#9aa0a6] bg-[#f8f9fa]">
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className={`h-8 w-8 rounded-full ${
                    viewMode === "list"
                      ? "bg-[#e8f0fe] text-[#174ea6] hover:bg-[#d2e3fc]"
                      : "text-[#5f6368] hover:bg-[#f1f3f4]"
                  }`}
                  onClick={() => setViewMode("list")}
                >
                  <List className="h-[18px] w-[18px]" />
                </Button>
              </TooltipTrigger>
              <TooltipContent><p>List view</p></TooltipContent>
            </Tooltip>
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className={`h-8 w-8 rounded-full ${
                    viewMode === "grid"
                      ? "bg-[#e8f0fe] text-[#174ea6] hover:bg-[#d2e3fc]"
                      : "text-[#5f6368] hover:bg-[#f1f3f4]"
                  }`}
                  onClick={() => setViewMode("grid")}
                >
                  <LayoutGrid className="h-[18px] w-[18px]" />
                </Button>
              </TooltipTrigger>
              <TooltipContent><p>Grid view</p></TooltipContent>
            </Tooltip>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full text-[#5f6368] hover:bg-[#f1f3f4]">
              <Info className="h-4 w-4" />
            </Button>
          </div>
        </TooltipProvider>
      </div>

      <div className="space-y-4 sm:space-y-6 pt-3 sm:pt-4">
      {/* Folders Section (grid view only – in list view, folders are inlined in FileList) */}
      {effectiveViewMode === "grid" && filteredFolders.length > 0 && (
        <section className="space-y-2">
          <FolderGrid folders={filteredFolders} />
        </section>
      )}

      {/* Suggested from your activity */}
      {!searchQuery && files.length > 0 && effectiveViewMode !== "list" && (
        <section className="hidden sm:block">
          <h2 className="text-xs sm:text-sm font-medium text-foreground mb-2 sm:mb-3">
            Suggested
          </h2>
          <SuggestedFiles files={recentFiles.slice(0, 6)} />
        </section>
      )}

      {/* File List with Tabs */}
      <section>
        <Tabs defaultValue="recent" className="w-full">
          {/* Tabs rendered inside FileList's filter bar row via topRightSlot;
              for grid view (no FileList) we still need them visible */}
          {effectiveViewMode === "grid" && (
            <div className="hidden sm:flex items-center justify-end mb-3">
              <TabsList className="h-8 bg-[#f1f3f4] rounded-full px-1">
                <TabsTrigger value="recent" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Recent</TabsTrigger>
                <TabsTrigger value="starred" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Starred</TabsTrigger>
              </TabsList>
            </div>
          )}

          <TabsContent value="recent">
            {effectiveViewMode === "grid" ? (
              filteredFiles.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-3">
                  {filteredFiles.map((file, index) => (
                    <FileCard key={file.id} file={file} priority={index < 12} />
                  ))}
                </div>
              ) : (
                <FileList files={filteredFiles} folders={filteredFolders} />
              )
            ) : (
              <FileList
                files={filteredFiles}
                folders={filteredFolders}
                stickyOffsetClass="top-12 sm:top-14"
                topRightSlot={
                  <TabsList className="h-8 bg-[#f1f3f4] rounded-full px-1">
                    <TabsTrigger value="recent" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Recent</TabsTrigger>
                    <TabsTrigger value="starred" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Starred</TabsTrigger>
                  </TabsList>
                }
              />
            )}
          </TabsContent>

          <TabsContent value="starred">
            {effectiveViewMode === "grid" ? (
              starredFiles.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-3">
                  {starredFiles.map((file, index) => (
                    <FileCard key={file.id} file={file} priority={index < 12} />
                  ))}
                </div>
              ) : (
                <FileList files={starredFiles} />
              )
            ) : (
              <FileList
                files={starredFiles}
                stickyOffsetClass="top-12 sm:top-14"
                topRightSlot={
                  <TabsList className="h-8 bg-[#f1f3f4] rounded-full px-1">
                    <TabsTrigger value="recent" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Recent</TabsTrigger>
                    <TabsTrigger value="starred" className="text-xs px-3.5 h-6 rounded-full font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">Starred</TabsTrigger>
                  </TabsList>
                }
              />
            )}
          </TabsContent>
        </Tabs>
      </section>
      </div>
    </div>
  );
}
