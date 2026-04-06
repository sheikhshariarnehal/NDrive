package com.ndrive.cloudvault.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndrive.cloudvault.domain.repository.AuthProfile
import com.ndrive.cloudvault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isConfigured: Boolean = true,
    val profile: AuthProfile? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val navigateToLogin: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            if (!authRepository.isConfigured()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isConfigured = false,
                        errorMessage = "Supabase is not configured. Set SUPABASE_URL and SUPABASE_ANON_KEY in local.properties."
                    )
                }
                return@launch
            }

            val profile = authRepository.getCurrentAuthProfile()
            if (profile == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = null,
                        errorMessage = "No active session found. Please sign in again.",
                        navigateToLogin = true
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.signOut()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Signed out successfully.",
                            navigateToLogin = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to sign out. Please try again."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToLogin = false) }
    }
}
