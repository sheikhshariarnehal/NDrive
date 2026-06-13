package com.ndrive.cloudvault.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndrive.cloudvault.domain.model.DriveFile
import com.ndrive.cloudvault.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhotosUiState(
        val isLoading: Boolean = true,
        val files: List<DriveFile> = emptyList(),
        val errorMessage: String? = null
)

@HiltViewModel
class PhotosViewModel @Inject constructor(
        private val fileRepository: FileRepository
) : ViewModel() {

        private val _uiState = MutableStateFlow(PhotosUiState())
        val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

        init {
                refresh()
        }

        fun refresh() {
                viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                        val result = fileRepository.getPhotoFiles(limit = 100)
                        _uiState.update {
                                it.copy(
                                        isLoading = false,
                                        files = result.getOrElse { emptyList() },
                                        errorMessage = result.exceptionOrNull()?.message
                                )
                        }
                }
        }
}
