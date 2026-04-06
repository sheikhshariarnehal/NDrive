package com.ndrive.cloudvault.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndrive.cloudvault.domain.model.DriveFile
import com.ndrive.cloudvault.domain.model.DriveFolder
import com.ndrive.cloudvault.domain.repository.FileRepository
import com.ndrive.cloudvault.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
	val isLoading: Boolean = true,
	val folders: List<DriveFolder> = emptyList(),
	val files: List<DriveFile> = emptyList(),
	val query: String = "",
	val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
	private val fileRepository: FileRepository,
	private val folderRepository: FolderRepository
) : ViewModel() {

	private val _uiState = MutableStateFlow(HomeUiState())
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

	init {
		refresh()
	}

	fun refresh() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, errorMessage = null) }

			val foldersDeferred = async { folderRepository.getRootFolders(limit = 20) }
			val filesDeferred = async { fileRepository.getRecentFiles(limit = 60) }

			val folderResult = foldersDeferred.await()
			val fileResult = filesDeferred.await()

			val error = folderResult.exceptionOrNull()?.message
				?: fileResult.exceptionOrNull()?.message

			_uiState.update {
				it.copy(
					isLoading = false,
					folders = folderResult.getOrElse { emptyList() },
					files = fileResult.getOrElse { emptyList() },
					errorMessage = error
				)
			}
		}
	}

	fun updateQuery(query: String) {
		_uiState.update { it.copy(query = query) }
	}

	fun clearError() {
		_uiState.update { it.copy(errorMessage = null) }
	}

	fun filteredFolders(): List<DriveFolder> {
		val q = _uiState.value.query.trim().lowercase()
		if (q.isBlank()) return _uiState.value.folders
		return _uiState.value.folders.filter { it.name.lowercase().contains(q) }
	}

	fun filteredFiles(): List<DriveFile> {
		val q = _uiState.value.query.trim().lowercase()
		if (q.isBlank()) return _uiState.value.files
		return _uiState.value.files.filter { it.name.lowercase().contains(q) }
	}
}
