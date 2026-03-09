import { S3Client, PutObjectCommand } from "@aws-sdk/client-s3";

const R2_ACCOUNT_ID = process.env.R2_ACCOUNT_ID || "";
const R2_ACCESS_KEY_ID = process.env.R2_ACCESS_KEY_ID || "";
const R2_SECRET_ACCESS_KEY = process.env.R2_SECRET_ACCESS_KEY || "";
const R2_BUCKET_NAME = process.env.R2_BUCKET_NAME || "cloudvault";
const R2_PUBLIC_URL = process.env.R2_PUBLIC_URL || "";

let _client: S3Client | null = null;

function getR2Client(): S3Client {
  if (!_client) {
    _client = new S3Client({
      region: "auto",
      endpoint: `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
      credentials: {
        accessKeyId: R2_ACCESS_KEY_ID,
        secretAccessKey: R2_SECRET_ACCESS_KEY,
      },
    });
  }
  return _client;
}

export function isR2Configured(): boolean {
  return !!(R2_ACCOUNT_ID && R2_ACCESS_KEY_ID && R2_SECRET_ACCESS_KEY && R2_PUBLIC_URL);
}

export function getThumbnailUrl(fileId: string): string {
  return `${R2_PUBLIC_URL}/thumbnails/${fileId}.jpg`;
}

/**
 * Upload a thumbnail buffer to R2.
 * @param fileId  Supabase file UUID — used as the R2 object key
 * @param buffer  Raw image bytes
 * @param contentType  MIME type (default: image/jpeg)
 * @returns  Public R2 URL for the thumbnail
 */
export async function uploadThumbnailToR2(
  fileId: string,
  buffer: Buffer,
  contentType: string = "image/jpeg",
): Promise<string> {
  const key = `thumbnails/${fileId}.jpg`;
  await getR2Client().send(
    new PutObjectCommand({
      Bucket: R2_BUCKET_NAME,
      Key: key,
      Body: buffer,
      ContentType: contentType,
      CacheControl: "public, max-age=31536000, immutable",
    }),
  );
  return getThumbnailUrl(fileId);
}
