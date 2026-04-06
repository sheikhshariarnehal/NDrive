package com.ndrive.cloudvault.domain.repository

import com.ndrive.cloudvault.domain.model.DriveFile

interface FileRepository {
	suspend fun getRecentFiles(limit: Int = 50): Result<List<DriveFile>>
}
