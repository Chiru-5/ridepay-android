package com.pm.ridepay.ui.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.repository.RideRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RideStatusUiState(
    val ride: RideStatusResponse? =null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isCancelling: Boolean = false,
    val actionMessage: String?=null
)

class RideStatusViewModel(
    private val rideRepository: RideRepository,
    private val rideId: Long
): ViewModel() {
    private val _uiState= MutableStateFlow(RideStatusUiState())
    val uiState: StateFlow<RideStatusUiState> = _uiState.asStateFlow()

    private var isRefreshing= false

    fun refresh(){
        if(isRefreshing) return
        isRefreshing = true
        viewModelScope.launch {
            try {
                rideRepository.getRideStatus(rideId)
                    .onSuccess { ride ->
                        _uiState.update {
                            it.copy(ride = ride, isLoading = false, errorMessage = null)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = error.toUserMessage())
                        }
                    }
            }finally {
                isRefreshing=false
            }
        }
    }

    fun cancelRide(){
        if(_uiState.value.isCancelling) return
        _uiState.update { it.copy(isCancelling = true, actionMessage = null) }
        viewModelScope.launch {
            rideRepository.cancelRide(rideId)
                .onSuccess {
                    ride -> _uiState.update { it.copy(ride=ride, isCancelling = false) }
                }
                .onFailure {
                    error-> _uiState.update {
                        it.copy(isCancelling = false, actionMessage = error.toUserMessage())
                }
                }
        }
    }
}

class RideStatusViewModelFactory(
    private val rideRepository: RideRepository,
    private val rideId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RideStatusViewModel(rideRepository, rideId) as T
}