package com.pm.ridepay.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.local.SessionEvents
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.VerificationStatus
import com.pm.ridepay.data.repository.AuthRepository
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface SessionState {
    data object Loading : SessionState
    data class LoggedOut(val notice: String? = null) : SessionState
    data class NeedsOnboarding(val driver: DriverStatusResponse) : SessionState
    data class ShowQr(val driver: DriverStatusResponse) : SessionState
    data class Ready(val driver: DriverStatusResponse) : SessionState
    data class Error(val message: String) : SessionState
}

class SessionViewModel(
    private val authRepository: AuthRepository,
    private val driverRepository: DriverRepository,
    sessionEvents: SessionEvents
) : ViewModel() {

    private val _state = MutableStateFlow<SessionState>(
        if (authRepository.isLoggedIn()) SessionState.Loading else SessionState.LoggedOut()
    )
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionEvents.expired.collect { expireSession() }
        }
        if (authRepository.isLoggedIn()) refresh()
    }

    fun onLoggedIn() = refresh()

    fun refresh() {
        _state.value = SessionState.Loading
        viewModelScope.launch {
            driverRepository.getStatus()
                .onSuccess { driver -> _state.value = routeFor(driver) }
                .onFailure { error ->
                    if (error is HttpException && error.code() == 401) {
                        expireSession()
                    } else {
                        _state.value = SessionState.Error(error.toUserMessage())
                    }
                }
        }
    }

    fun onOnboarded(driver: DriverStatusResponse) {
        _state.value = SessionState.ShowQr(driver)
    }

    fun onQrContinue() {
        val current = _state.value
        if (current is SessionState.ShowQr) {
            _state.value = SessionState.Ready(current.driver)
        }
    }

    fun logout() {
        authRepository.logout()
        _state.value = SessionState.LoggedOut()
    }

    // Safe to call several times: only the first one has any effect
    private fun expireSession() {
        if (_state.value is SessionState.LoggedOut) return
        authRepository.logout()
        _state.value = SessionState.LoggedOut("Your session expired. Please log in again.")
    }

    private fun routeFor(driver: DriverStatusResponse): SessionState =
        if (driver.verificationStatus == VerificationStatus.VERIFIED) {
            SessionState.Ready(driver)
        } else {
            SessionState.NeedsOnboarding(driver)
        }
}

class SessionViewModelFactory(
    private val authRepository: AuthRepository,
    private val driverRepository: DriverRepository,
    private val sessionEvents: SessionEvents
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SessionViewModel(authRepository, driverRepository, sessionEvents) as T
}