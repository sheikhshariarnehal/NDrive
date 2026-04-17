package com.ndrive.cloudvault.presentation.upload

import androidx.lifecycle.ViewModel
import com.ndrive.cloudvault.presentation.home.UploadPanelStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class UploadViewModel @Inject constructor(
	private val uploadPanelStore: UploadPanelStore,
) : ViewModel() {

	val panelState: StateFlow<com.ndrive.cloudvault.presentation.home.UploadPanelState> = uploadPanelStore.state

	fun clearAll() {
		uploadPanelStore.clearAll()
	}

	fun toggleExpanded() {
		uploadPanelStore.toggleExpanded()
	}

	fun dismissItem(itemId: Long) {
		uploadPanelStore.dismissItem(itemId)
	}
}
