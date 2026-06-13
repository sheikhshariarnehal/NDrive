package com.ndrive.cloudvault.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndrive.cloudvault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination {
    LOADING,
    HOME,
    LOGIN,
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            // Ensure the splash screen is visible for at least 1 second to prevent flicker
            val minSplashTime = 1000L
            val startTime = System.currentTimeMillis()

            val hasSession = if (!authRepository.isConfigured()) {
                false
            } else {
                runCatching { authRepository.hasActiveSession() }.getOrDefault(false)
            }

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < minSplashTime) {
                delay(minSplashTime - elapsed)
            }

            _destination.value = if (hasSession) {
                SplashDestination.HOME
            } else {
                SplashDestination.LOGIN
            }
        }
    }
}
