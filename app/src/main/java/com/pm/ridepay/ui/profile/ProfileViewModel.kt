package com.pm.ridepay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.DriverProfileResponse
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: DriverProfileResponse? =null,
    val errorMessage: String? =null
)


class ProfileViewModel(
    private val driverRepository: DriverRepository
): ViewModel() {

    private val _uiState= MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load(){
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            driverRepository.getProfile()
                .onSuccess { profile -> _uiState.value = ProfileUiState(isLoading = false, profile = profile)
                }
                .onFailure { error->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }
}

class ProfileViewModelFactory(
    private val driverRepository: DriverRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(driverRepository) as T
}