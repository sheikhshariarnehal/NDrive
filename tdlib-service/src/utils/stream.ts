import fs from "fs";
import { promises as fsPromises } from "fs";
import { Response } from "express";
import mime from "mime-types";
import path from "path";

const KB = 1024;
const MIN_PROGRESSIVE_CHUNK_BYTES = 64 * KB;
const DEFAULT_PROGRESSIVE_CHUNK_BYTES = 256 * KB;
const DEFAULT_PROGRESSIVE_POLL_INTERVAL_MS = 100;
const DEFAULT_PROGRESSIVE_STALL_TIMEOUT_MS = 90_000;

const USE_READFILEPART = process.env.DOWNLOAD_USE_READFILEPART === "true";

function parsePositiveInt(value: string | undefined, fallback: number): number {
  if (!value) return fallback;
  const parsed = parseInt(value, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const PROGRESSIVE_CHUNK_BYTES = Math.max(
  MIN_PROGRESSIVE_CHUNK_BYTES,
  parsePositiveInt(process.env.DOWNLOAD_PROGRESSIVE_CHUNK_KB, DEFAULT_PROGRESSIVE_CHUNK_BYTES / KB) * KB,
);
const PROGRESSIVE_POLL_INTERVAL_MS = parsePositiveInt(
  process.env.DOWNLOAD_PROGRESSIVE_POLL_INTERVAL_MS,
  DEFAULT_PROGRESSIVE_POLL_INTERVAL_MS,
);
const PROGRESSIVE_STALL_TIMEOUT_MS = parsePositiveInt(
  process.env.DOWNLOAD_PROGRESSIVE_STALL_TIMEOUT_MS,
  DEFAULT_PROGRESSIVE_STALL_TIMEOUT_MS,
);

export interface ProgressInfo {
  getDownloadedSize: () => number;
  getExpectedSize: () => number;
  isComplete: () => boolean;
  getError: () => Error | undefined;
}

/** TDLib client subset needed for readFilePart */
export interface TdClientRef {
  invoke: (params: Record<string, unknown>) => Promise<any>;
}

/** Parsed result of a valid `Range: bytes=<start>-<end>` header. */
interface ParsedRange {
  start: number;
  end: number;
}

/**
 * Parse a `Range: bytes=<start>-[end]` header against a known total size.
 * Returns null if the header is absent, malformed, or if the range is
 * unsatisfiable (start >= totalSize).
 */
function parseRangeHeader(
  rangeHeader: string | undefined,
  totalSize: number,
): ParsedRange | null {
  if (!rangeHeader || totalSize <= 0) return null;
  const parts = rangeHeader.replace(/bytes=/, "").split("-");
  const start = parseInt(parts[0], 10);
  if (isNaN(start) || start < 0 || start >= totalSize) return null;
  const end = parts[1] ? Math.min(parseInt(parts[1], 10), totalSize - 1) : totalSize - 1;
  if (isNaN(end) || end < start) return null;
  return { start, end };
}

/**
 * Stream a local file to an Express response progressively as it downloads.
 * Supports Range requests and optional TDLib readFilePart for byte reads.
 */
export async function streamFileProgressively(
  filePath: string,
  res: Response,
  progressObj: ProgressInfo,
  options: {
    fileName?: string;
    mimeType?: string;
    inline?: boolean;
    rangeHeader?: string;
    tdClient?: TdClientRef;
    tdFileId?: number;
  } = {}
): Promise<void> {
  const expectedSize = progressObj.getExpectedSize();
  const contentType = options.mimeType || mime.lookup(filePath) || "application/octet-stream";
  const disposition = options.inline ? "inline" : "attachment";
  const fileName = options.fileName || path.basename(filePath);

  const range = parseRangeHeader(options.rangeHeader, expectedSize);

  const rangeStart = range?.start ?? 0;
  const rangeEnd = range?.end ?? (expectedSize > 0 ? expectedSize - 1 : 0);

  if (range) {
    res.status(206).set({
      "Content-Range": `bytes ${range.start}-${range.end}/${expectedSize}`,
      "Accept-Ranges": "bytes",
      "Content-Length": (range.end - range.start + 1).toString(),
      "Content-Type": contentType,
      "Content-Disposition": `${disposition}; filename="${encodeURIComponent(fileName)}"`,
    });
  } else {
    res.set({
      "Content-Length": expectedSize.toString(),
      "Content-Type": contentType,
      "Accept-Ranges": "bytes",
      "Content-Disposition": `${disposition}; filename="${encodeURIComponent(fileName)}"`,
      "Cache-Control": "private, max-age=3600",
    });
  }

  if (expectedSize <= 0) {
    res.end();
    return;
  }

  const useTdRead = USE_READFILEPART && options.tdClient && options.tdFileId != null;

  let fd: fsPromises.FileHandle | null = null;
  try {
    if (!useTdRead) {
      fd = await fsPromises.open(filePath, 'r');
    }
    const buffer = useTdRead ? null : Buffer.alloc(PROGRESSIVE_CHUNK_BYTES);
    let position = rangeStart;
    const endPosition = rangeEnd + 1;
    const maxConsecutiveStalls = Math.max(
      1,
      Math.ceil(PROGRESSIVE_STALL_TIMEOUT_MS / PROGRESSIVE_POLL_INTERVAL_MS),
    );

    let consecutiveStalls = 0;
    while (position < endPosition) {
      if (res.closed) break;

      const err = progressObj.getError();
      if (err) throw err;

      const downloadedSize = progressObj.getDownloadedSize();
      const availableBytes = downloadedSize - position;

      if (availableBytes > 0) {
        consecutiveStalls = 0;
        const bytesToRead = Math.min(availableBytes, PROGRESSIVE_CHUNK_BYTES, endPosition - position);

        let chunk: Buffer;
        if (useTdRead) {
          const result = await options.tdClient!.invoke({
            _: "readFilePart",
            file_id: options.tdFileId!,
            offset: position,
            count: bytesToRead,
          });
          chunk = Buffer.from(result.data as string, "base64");
        } else {
          const { bytesRead } = await fd!.read(buffer!, 0, bytesToRead, position);
          if (bytesRead === 0) continue;
          chunk = buffer!.subarray(0, bytesRead);
        }

        if (chunk.length > 0) {
          const canWrite = res.write(chunk);
          position += chunk.length;
          if (!canWrite) {
            await new Promise(resolve => res.once('drain', resolve));
          }
        }
      } else {
        if (progressObj.isComplete() && position >= endPosition) {
          break;
        }

        consecutiveStalls++;
        if (consecutiveStalls > maxConsecutiveStalls) {
          throw new Error("Download stalled for too long");
        }
        await new Promise(resolve => setTimeout(resolve, PROGRESSIVE_POLL_INTERVAL_MS));
      }
    }
    if (!res.closed) {
      res.end();
    }
  } finally {
    if (fd) await fd.close();
  }
}

/**
 * Stream a local file to an Express response with proper headers.
 * Supports Range requests for video seeking / resumable downloads.
 */
export function streamFileToResponse(
  filePath: string,
  res: Response,
  options: {
    fileName?: string;
    mimeType?: string;
    inline?: boolean;
    rangeHeader?: string;
  } = {}
): void {
  const stat = fs.statSync(filePath);
  const fileSize = stat.size;

  const contentType =
    options.mimeType ||
    mime.lookup(filePath) ||
    "application/octet-stream";

  const disposition = options.inline ? "inline" : "attachment";
  const fileName = options.fileName || path.basename(filePath);

  const range = parseRangeHeader(options.rangeHeader, fileSize);

  if (range) {
    if (range.start >= fileSize || range.end >= fileSize) {
      res.status(416).set("Content-Range", `bytes */${fileSize}`).end();
      return;
    }

    res.status(206).set({
      "Content-Range": `bytes ${range.start}-${range.end}/${fileSize}`,
      "Accept-Ranges": "bytes",
      "Content-Length": (range.end - range.start + 1).toString(),
      "Content-Type": contentType,
      "Content-Disposition": `${disposition}; filename="${encodeURIComponent(fileName)}"`,
    });

    fs.createReadStream(filePath, { start: range.start, end: range.end }).pipe(res);
  } else {
    res.set({
      "Content-Length": fileSize.toString(),
      "Content-Type": contentType,
      "Accept-Ranges": "bytes",
      "Content-Disposition": `${disposition}; filename="${encodeURIComponent(fileName)}"`,
      "Cache-Control": "private, max-age=3600",
    });

    fs.createReadStream(filePath).pipe(res);
  }
}

/**
 * Read a file and return it as a base64 data URI.
 * Rejects files larger than 50 MB to prevent OOM.
 */
export function fileToBase64DataUri(
  filePath: string,
  mimeType?: string
): string {
  const stat = fs.statSync(filePath);
  if (stat.size > 50 * 1024 * 1024) {
    throw new Error(`File too large for base64 conversion (${Math.round(stat.size / 1024 / 1024)} MB)`);
  }
  const buffer = fs.readFileSync(filePath);
  const type = mimeType || mime.lookup(filePath) || "application/octet-stream";
  return `data:${type};base64,${buffer.toString("base64")}`;
}
