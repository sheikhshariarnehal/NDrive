import { generateThumbnail, type ThumbnailOptions } from "@/lib/telegram/thumbnail";
import { uploadThumbnail, isR2Configured } from "@/lib/r2";
import { SupabaseClient } from "@supabase/supabase-js";

const VISUAL_MIME_PREFIXES = ["image/", "video/"];

function isVisualMime(mimeType?: string | null): boolean {
  return !!mimeType && VISUAL_MIME_PREFIXES.some((prefix) => mimeType.startsWith(prefix));
}

async function persistClientThumbnail(
  supabase: SupabaseClient,
  fileId: string,
  clientThumbnailBase64: string,
  logLabel?: string,
): Promise<string | null> {
  try {
    const buffer = Buffer.from(clientThumbnailBase64, "base64");
    if (!buffer.length) return null;

    const r2Url = await uploadThumbnail(fileId, buffer, "image/jpeg");
    const { error } = await supabase
      .from("files")
      .update({ thumbnail_url: r2Url })
      .eq("id", fileId);

    if (error) {
      console.error("[Upload] Client thumbnail DB update failed:", error.message);
      return null;
    }

    console.log(`[Upload] Client thumbnail saved to R2 for ${logLabel || fileId}`);
    return r2Url;
  } catch (thumbErr) {
    console.error("[Upload] Client thumbnail R2 upload failed:", thumbErr);
    return null;
  }
}

/**
 * Generate and persist a thumbnail for a newly uploaded file.
 *
 * 1. Try Telegram-based thumbnail (TDLib extracts from the uploaded media)
 * 2. Fallback: use the client-generated base64 thumbnail from the browser
 *
 * Non-fatal — returns the R2 URL if successful, null otherwise.
 * Mutates fileRecord.thumbnail_url in place when a URL is obtained.
 */
export async function resolveUploadThumbnail(
  supabase: SupabaseClient,
  fileRecord: {
    id: string;
    mime_type?: string | null;
    telegram_message_id?: number | null;
    storage_type?: string | null;
    telegram_chat_id?: number | null;
    thumbnail_url?: string | null;
  },
  opts: {
    storageType: string;
    userId: string | null;
    clientThumbnail: string | null;
    logLabel?: string;
  },
): Promise<string | null> {
  const hasVisualMime = isVisualMime(fileRecord.mime_type);
  const canUseClientFallback = !!opts.clientThumbnail && isR2Configured();

  if (!hasVisualMime && !canUseClientFallback) {
    return null;
  }

  let r2Url: string | null = null;

  // 1) Persist client thumbnail first when available. This guarantees
  // a usable thumbnail even if Telegram processing is delayed.
  if (canUseClientFallback && opts.clientThumbnail) {
    r2Url = await persistClientThumbnail(
      supabase,
      fileRecord.id,
      opts.clientThumbnail,
      opts.logLabel,
    );
  }

  // 2) Try Telegram-based thumbnail and let it replace the fallback
  // with a potentially better-quality frame when available.
  if (hasVisualMime && fileRecord.telegram_message_id) {
    const thumbOpts: ThumbnailOptions = {
      storageType: fileRecord.storage_type || opts.storageType,
      userId: opts.userId,
      chatId: fileRecord.telegram_chat_id,
    };

    const telegramR2Url = await generateThumbnail(
      fileRecord.id,
      fileRecord.telegram_message_id,
      thumbOpts,
    );

    if (telegramR2Url) {
      r2Url = telegramR2Url;
    }
  }

  if (r2Url) fileRecord.thumbnail_url = r2Url;
  return r2Url;
}
