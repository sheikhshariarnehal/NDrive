package com.ndrive.cloudvault.presentation.home

import com.ndrive.cloudvault.domain.model.UploadPhase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class UploadPanelStore @Inject constructor() {

    private val _state = MutableStateFlow(UploadPanelState())
    val state: StateFlow<UploadPanelState> = _state.asStateFlow()

    fun startQueue(queueItems: List<UploadItemUiState>) {
        _state.value = UploadPanelState(
            isVisible = true,
            isExpanded = true,
            isRunning = true,
            items = queueItems,
            summaryMessage = null,
            successCount = 0,
            failedCount = 0,
        )
    }

    fun updateItem(
        itemId: Long,
        status: UploadItemStatus,
        phase: UploadPhase?,
        progressPercent: Int,
        message: String?,
        retryAfterSeconds: Int?,
    ) {
        _state.update { current ->
            val updatedItems = current.items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        status = status,
                        phase = phase,
                        progressPercent = progressPercent,
                        message = message,
                        retryAfterSeconds = retryAfterSeconds,
                    )
                } else {
                    item
                }
            }

            current.copy(
                items = updatedItems,
                isVisible = true,
            )
        }
    }

    fun finishQueue(successCount: Int, failedCount: Int, totalCount: Int) {
        val summaryMessage = when {
            totalCount <= 0 -> "No uploads"
            failedCount == 0 -> "Uploaded $successCount of $totalCount files"
            successCount == 0 -> "Uploaded 0 of $totalCount files. $failedCount failed"
            else -> "Uploaded $successCount of $totalCount files. $failedCount failed"
        }

        _state.update { current ->
            current.copy(
                isVisible = true,
                isExpanded = failedCount > 0,
                isRunning = false,
                summaryMessage = summaryMessage,
                successCount = successCount,
                failedCount = failedCount,
            )
        }
    }

    fun toggleExpanded() {
        _state.update { current ->
            if (!current.isVisible || current.items.isEmpty()) {
                current
            } else {
                current.copy(isExpanded = !current.isExpanded)
            }
        }
    }

    fun dismissItem(itemId: Long) {
        _state.update { current ->
            val item = current.items.firstOrNull { it.id == itemId } ?: return@update current
            if (item.status == UploadItemStatus.UPLOADING || item.status == UploadItemStatus.QUEUED) {
                return@update current
            }

            val remaining = current.items.filterNot { it.id == itemId }
            if (remaining.isEmpty() && !current.isRunning) {
                UploadPanelState()
            } else {
                current.copy(items = remaining)
            }
        }
    }

    fun clearAll() {
        _state.value = UploadPanelState()
    }
}
