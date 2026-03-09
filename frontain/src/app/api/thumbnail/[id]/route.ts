import { NextRequest, NextResponse } from "next/server";
import { createClient } from "@/lib/supabase/server";
import { createClient as createSupabaseClient } from "@supabase/supabase-js";

const BACKEND_URL = process.env.TDLIB_SERVICE_URL || "http://localhost:3001";
const API_KEY = process.env.TDLIB_SERVICE_API_KEY || "";
const CHANNEL_ID = process.env.TELEGRAM_CHANNEL_ID || "";

/**
 * GET /api/thumbnail/[id]
 *
 * Serves the thumbnail for a file:
 *   1. If thumbnail_url is an R2 URL → 302 redirect (fast, no Vercel compute)
 *   2. If thumbnail_url is a base64 data-URI (legacy) → re-fetch from TDLib backend
 *      which uploads to R2 and returns r2_url → update DB → redirect
 *   3. If missing → fetch from TDLib backend (backend uploads to R2) → update DB → redirect
 *   4. Fallback: serve base64 inline if backend returns no r2_url
 */
export const dynamic = "force-dynamic";

function isR2Url(url: string): boolean {
  return url.startsWith("https://");
}

function isBase64DataUri(url: string): boolean {
  return url.startsWith("data:");
}

function parseDataUri(dataUri: string): { contentType: string; buffer: Buffer } | null {
  const match = dataUri.match(/^data:([^;]+);base64,(.+)$/);
  if (!match) return null;
  return { contentType: match[1], buffer: Buffer.from(match[2], "base64") };
}

function getServiceClient() {
  return createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.SUPABASE_SERVICE_ROLE_KEY!,
  );
}

/**
 * Call the TDLib backend to fetch+upload thumbnail for a message.
 * Backend uploads to R2 if configured and file_id is provided.
 * Returns { r2_url, thumbnail } from the backend response.
 */
async function fetchFromBackend(
  telegramMessageId: number,
  fileId: string,
): Promise<{ r2_url: string | null; thumbnail: string | null }> {
  try {
    const resp = await fetch(`${BACKEND_URL}/api/thumbnail/from-message`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-API-Key": API_KEY },
      body: JSON.stringify({
        chat_id: CHANNEL_ID,
        message_id: telegramMessageId,
        file_id: fileId,
      }),
    });
    if (!resp.ok) return { r2_url: null, thumbnail: null };
    const data = await resp.json();
    return {
      r2_url: data.r2_url ?? null,
      thumbnail: data.thumbnail ?? null,
    };
  } catch {
    return { r2_url: null, thumbnail: null };
  }
}

/** Persist the R2 URL to Supabase (fire-and-forget). */
function saveR2UrlToDb(fileId: string, r2Url: string): void {
  getServiceClient()
    .from("files")
    .update({ thumbnail_url: r2Url })
    .eq("id", fileId)
    .then(({ error }) => {
      if (error) console.error("[Thumbnail] DB update failed:", error.message);
    });
}

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  if (!id) {
    return NextResponse.json({ error: "File ID required" }, { status: 400 });
  }

  try {
    const supabase = await createClient();

    const { data: file, error } = await supabase
      .from("files")
      .select("thumbnail_url, telegram_message_id, mime_type")
      .eq("id", id)
      .single();

    if (error || !file) {
      return new NextResponse(null, { status: 404 });
    }

    // ── Fast path: already an R2 URL → instant redirect ──
    if (file.thumbnail_url && isR2Url(file.thumbnail_url)) {
      return NextResponse.redirect(file.thumbnail_url, 302);
    }

    // ── Need to fetch from TDLib backend (handles R2 upload server-side) ──
    if (!file.telegram_message_id || !CHANNEL_ID) {
      // Legacy base64 fallback if no message ID
      if (file.thumbnail_url && isBase64DataUri(file.thumbnail_url)) {
        const parsed = parseDataUri(file.thumbnail_url);
        if (parsed) {
          const body = new Uint8Array(parsed.buffer);
          return new NextResponse(body, {
            headers: {
              "Content-Type": parsed.contentType,
              "Content-Length": String(body.length),
              "Cache-Control": "public, max-age=604800, immutable",
            },
          });
        }
      }
      return new NextResponse(null, { status: 404 });
    }

    const { r2_url, thumbnail } = await fetchFromBackend(file.telegram_message_id, id);

    // Backend uploaded to R2 → persist URL and redirect
    if (r2_url) {
      saveR2UrlToDb(id, r2_url);
      return NextResponse.redirect(r2_url, 302);
    }

    // Backend returned base64 fallback (R2 not configured on backend)
    if (thumbnail && isBase64DataUri(thumbnail)) {
      const parsed = parseDataUri(thumbnail);
      if (parsed) {
        const body = new Uint8Array(parsed.buffer);
        return new NextResponse(body, {
          headers: {
            "Content-Type": parsed.contentType,
            "Content-Length": String(body.length),
            "Cache-Control": "public, max-age=604800, immutable",
          },
        });
      }
    }

    return new NextResponse(null, { status: 404 });
  } catch {
    return new NextResponse(null, { status: 500 });
  }
}

