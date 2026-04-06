package com.ndrive.cloudvault.data.repository

import com.ndrive.cloudvault.domain.model.DriveFile
import com.ndrive.cloudvault.domain.repository.FileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Singleton
class FileRepositoryImpl @Inject constructor(
	private val supabaseClient: SupabaseClient
) : FileRepository {

	override suspend fun getRecentFiles(limit: Int): Result<List<DriveFile>> = runCatching {
		supabaseClient
			.from("files")
			.select {
				filter {
					eq("is_trashed", false)
				}
				order(column = "updated_at", order = Order.DESCENDING)
				limit(limit.toLong())
			}
			.decodeList<FileRow>()
			.map { row ->
				DriveFile(
					id = row.id,
					name = row.name,
					mimeType = row.mimeType,
					sizeBytes = row.sizeBytes,
					folderId = row.folderId,
					updatedAt = row.updatedAt,
					isStarred = row.isStarred,
					thumbnailUrl = row.thumbnailUrl
				)
			}
	}

	@Serializable
	@JsonIgnoreUnknownKeys
	private data class FileRow(
		val id: String,
		val name: String,
		@SerialName("mime_type") val mimeType: String,
		@SerialName("size_bytes") val sizeBytes: Long,
		@SerialName("folder_id") val folderId: String? = null,
		@SerialName("updated_at") val updatedAt: String? = null,
		@SerialName("is_starred") val isStarred: Boolean = false,
		@SerialName("thumbnail_url") val thumbnailUrl: String? = null
	)
}
