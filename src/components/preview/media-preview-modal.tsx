"use client";

import { useEffect, useState, useCallback } from "react";
import { useFilesStore } from "@/store/files-store";
import { useUIStore } from "@/store/ui-store";
import { ImagePreview } from "@/components/preview/image-preview";
import { PdfPreview } from "@/components/preview/pdf-preview";
import { VideoPreview } from "@/components/preview/video-preview";
import { OfficePreview } from "@/components/preview/office-preview";
import { CsvPreview } from "@/components/preview/csv-preview";
import { TextPreview } from "@/components/preview/text-preview";
import { JsonPreview } from "@/components/preview/json-preview";
import { PptxPreview } from "@/components/preview/pptx-preview";
import {
  Download,
  FileIcon,
  ChevronLeft,
  ChevronRight,
  Share2,
  MoreVertical,
  ArrowLeft,
  SlidersHorizontal,
  ZoomIn,
  Info,
  Star,
  Trash2,
} from "lucide-react";
import { getFileCategory, formatFileSize, isOfficeFile, isCsvFile, isPptxFile, isJsonFile, isTextFile, isPreviewableFile, isLegacyPptFile } from "@/types/file.types";
import { getFileUrl } from "@/lib/utils";

export function MediaPreviewModal() {
  const { files } = useFilesStore();
  const { previewFileId, setPreviewFileId, shareModalOpen, setShareModalOpen, setShareFileId } =
    useUIStore();
  const [fileUrl, setFileUrl] = useState<string | null>(null);

  const file = files.find((f) => f.id === previewFileId);

  // Get only previewable image files for navigation
  const imageFiles = files.filter(
    (f) => getFileCategory(f.mime_type) === "image"
  );
  const currentImageIndex = imageFiles.findIndex(
    (f) => f.id === previewFileId
  );
  const category = file ? getFileCategory(file.mime_type) : null;
  const isImage = category === "image";

  // Build the file URL when the preview file changes
  useEffect(() => {
    if (!previewFileId || !file) {
      setFileUrl(null);
      return;
    }
    setFileUrl(getFileUrl(previewFileId, file.name));
  }, [previewFileId, file]);

  // Keyboard navigation
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (!previewFileId || shareModalOpen) return;

      if (e.key === "Escape") {
        setPreviewFileId(null);
      } else if (e.key === "ArrowLeft" && isImage && currentImageIndex > 0) {
        setPreviewFileId(imageFiles[currentImageIndex - 1].id);
      } else if (
        e.key === "ArrowRight" &&
        isImage &&
        currentImageIndex < imageFiles.length - 1
      ) {
        setPreviewFileId(imageFiles[currentImageIndex + 1].id);
      }
    },
    [previewFileId, isImage, currentImageIndex, imageFiles, setPreviewFileId, shareModalOpen]
  );

  useEffect(() => {
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  // Lock body scroll when modal is open
  useEffect(() => {
    if (previewFileId) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [previewFileId]);

  if (!file || !previewFileId) return null;

  const handleDownload = () => {
    window.open(getFileUrl(file.id, file.name, true), "_blank");
  };

  const handleShare = () => {
    setShareFileId(file.id);
    setShareModalOpen(true);
  };

  // Print removed to match Google Photos cleaner top-bar experience
  // but keeping handleDownload below intact.

  const handlePrev = () => {
    if (currentImageIndex > 0) {
      setPreviewFileId(imageFiles[currentImageIndex - 1].id);
    }
  };

  const handleNext = () => {
    if (currentImageIndex < imageFiles.length - 1) {
      setPreviewFileId(imageFiles[currentImageIndex + 1].id);
    }
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      setPreviewFileId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex flex-col bg-black">
      {/* ===== TOP BAR - Google Photos style ===== */}
      <div className="absolute top-0 left-0 right-0 z-50 flex items-start justify-between h-24 pt-3 sm:pt-4 px-2 sm:px-4 bg-gradient-to-b from-black/60 via-black/10 to-transparent pointer-events-none transition-opacity duration-300">
        
        {/* Left: Back Arrow */}
        <div className="flex items-center pointer-events-auto">
          <button
            onClick={() => setPreviewFileId(null)}
            className="p-2 sm:p-2.5 text-white bg-black/40 hover:bg-black/60 backdrop-blur-sm rounded-full transition-all flex-shrink-0 shadow-sm"
            title="Back"
          >
            <ArrowLeft className="h-6 w-6" />
          </button>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-0.5 sm:gap-1 pointer-events-auto">
          <button
            onClick={handleShare}
            className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors"
            title="Share"
          >
            <Share2 className="h-5 w-5" />
          </button>
          
          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors hidden sm:block" title="Edit">
            <SlidersHorizontal className="h-5 w-5" />
          </button>
          
          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors hidden sm:block" title="Zoom">
            <ZoomIn className="h-5 w-5" />
          </button>
          
          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors hidden md:block" title="Info">
            <Info className="h-5 w-5" />
          </button>
          
          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors hidden md:block" title="Star">
            <Star className="h-5 w-5" />
          </button>

          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors hidden sm:block" title="Delete">
            <Trash2 className="h-5 w-5" />
          </button>
          
          {/* We keep Download prominently since it's a Cloud Drive */}
          <button
            onClick={handleDownload}
            className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors"
            title="Download"
          >
            <Download className="h-5 w-5" />
          </button>
          
          <button className="p-2 sm:p-2.5 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors" title="More options">
            <MoreVertical className="h-5 w-5" />
          </button>
        </div>
      </div>

      {/* ===== MAIN CONTENT AREA ===== */}
      <div
        className="flex-1 relative flex items-center justify-center overflow-hidden w-full h-full"
        onClick={handleOverlayClick}
      >
        {/* Left navigation arrow */}
        {isImage && currentImageIndex > 0 && (
          <button
            onClick={handlePrev}
            className="absolute left-4 z-10 p-3 text-white/60 hover:text-white bg-black/30 hover:bg-black/50 rounded-full transition-all"
            title="Previous"
          >
            <ChevronLeft className="h-7 w-7" />
          </button>
        )}

        {/* Preview content */}
        <div className="w-full h-full flex items-center justify-center">
          {fileUrl && (
            <>
              {isImage && <ImagePreview src={fileUrl} alt={file.name} fallbackSrc={file.thumbnail_url} />}
              {category === "pdf" && (
                <div className="absolute inset-0 w-full h-full">
                  <PdfPreview src={fileUrl} />
                </div>
              )}
              {category === "video" && (
                <div className="absolute inset-0 w-full h-full">
                  <VideoPreview src={fileUrl} />
                </div>
              )}
              {category === "audio" && (
                <div className="p-8 text-center">
                  <FileIcon className="h-20 w-20 text-white/30 mx-auto mb-6" />
                  <p className="text-lg font-medium mb-6 text-white">
                    {file.name}
                  </p>
                  <audio controls src={fileUrl} className="w-full max-w-md" />
                </div>
              )}
              {/* Office documents (Word, Excel) — NOT PowerPoint */}
              {isOfficeFile(file.mime_type, file.name) && !isPptxFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <OfficePreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* PowerPoint presentations (.pptx) */}
              {isPptxFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <PptxPreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* Legacy PowerPoint (.ppt) — show friendly download prompt */}
              {isLegacyPptFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <PptxPreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* CSV files */}
              {isCsvFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <CsvPreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* JSON files */}
              {isJsonFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <JsonPreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* Text / code files (.md, .sql, .html, .css, .js, .py, .txt, etc.) */}
              {isTextFile(file.mime_type, file.name) && (
                <div className="absolute inset-0 w-full h-full">
                  <TextPreview src={fileUrl} fileName={file.name} onDownload={handleDownload} />
                </div>
              )}
              {/* Other non-previewable files */}
              {!isImage && category !== "pdf" && category !== "video" && category !== "audio" &&
                !isPreviewableFile(file.mime_type, file.name) ? (
                <div className="p-8 text-center">
                  <FileIcon className="h-20 w-20 text-white/30 mx-auto mb-6" />
                  <p className="text-lg font-medium mb-2 text-white">
                    {file.name}
                  </p>
                  <p className="text-sm text-white/50 mb-1">
                    {file.mime_type}
                  </p>
                  <p className="text-sm text-white/50 mb-6">
                    Preview is not available for this file type. Download the file to view it.
                  </p>
                  <button
                    onClick={handleDownload}
                    className="inline-flex items-center gap-2 px-6 py-3 bg-[#8ab4f8] text-[#202124] rounded-full font-medium hover:bg-[#aecbfa] transition-colors"
                  >
                    <Download className="h-4 w-4" />
                    Download File
                  </button>
                </div>
              ) : null}
            </>
          )}
        </div>

        {/* Right navigation arrow */}
        {isImage && currentImageIndex < imageFiles.length - 1 && (
          <button
            onClick={handleNext}
            className="absolute right-4 z-10 p-3 text-white/60 hover:text-white bg-black/30 hover:bg-black/50 rounded-full transition-all"
            title="Next"
          >
            <ChevronRight className="h-7 w-7" />
          </button>
        )}
      </div>

      {/* Image counter for multi-image navigation */}
      {isImage && imageFiles.length > 1 && (
        <div className="absolute bottom-6 right-6 text-xs text-white/50 bg-[#2d2e30] px-3 py-1.5 rounded-full">
          {currentImageIndex + 1} / {imageFiles.length}
        </div>
      )}
    </div>
  );
}
