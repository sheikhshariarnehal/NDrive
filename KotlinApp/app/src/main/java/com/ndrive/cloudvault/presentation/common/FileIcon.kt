package com.ndrive.cloudvault.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

data class FileIconStyle(
	val icon: ImageVector,
	val tint: Color,
	val prefersMediaPreview: Boolean = false,
)

fun resolveFileIconStyle(name: String, mimeType: String?): FileIconStyle {
	val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
	val mime = mimeType.orEmpty().lowercase(Locale.ROOT)

	val isImage = mime.startsWith("image/") || extension in setOf(
		"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic", "heif",
	)
	if (isImage) {
		return FileIconStyle(
			icon = Icons.Default.Image,
			tint = Color(0xFF1A73E8),
			prefersMediaPreview = true,
		)
	}

	val isVideo = mime.startsWith("video/") || extension in setOf(
		"mp4", "mov", "mkv", "avi", "webm", "3gp",
	)
	if (isVideo) {
		return FileIconStyle(
			icon = Icons.Default.Slideshow,
			tint = Color(0xFF0F9D58),
			prefersMediaPreview = true,
		)
	}

	if (mime == "application/pdf" || extension == "pdf") {
		return FileIconStyle(Icons.Default.PictureAsPdf, Color(0xFFEA4335))
	}

	if (
		mime.contains("spreadsheet") ||
		extension in setOf("xls", "xlsx", "csv", "ods")
	) {
		return FileIconStyle(Icons.Default.TableChart, Color(0xFF34A853))
	}

	if (
		mime.contains("presentation") ||
		extension in setOf("ppt", "pptx", "odp", "key")
	) {
		return FileIconStyle(Icons.Default.Slideshow, Color(0xFFFB8C00))
	}

	if (
		mime.contains("word") ||
		mime.startsWith("text/") ||
		extension in setOf("doc", "docx", "txt", "rtf", "md", "odt")
	) {
		return FileIconStyle(Icons.Default.Description, Color(0xFF4285F4))
	}

	return FileIconStyle(Icons.Default.Description, Color(0xFF5F6368))
}
