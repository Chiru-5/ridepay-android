package com.pm.ridepay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.model.RideStatus
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.repository.RideRepository
import com.pm.ridepay.util.isToday
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal


data class HomeUiState(
    val isLoading: Boolean =true,
    val todaysRides: List<RideHistoryItem> = emptyList(),
    val totalCollected: BigDecimal = BigDecimal.ZERO,
    val activeRide: RideStatusResponse? =null,
    val errorMessage: String? =null
)

class HomeViewModel(
    private val rideRepository: RideRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    fun load(){
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch{
            val history = rideRepository.getHistory()
            val active = rideRepository.getActiveRide()

            val failure = history.exceptionOrNull() ?: active.exceptionOrNull()
            if(failure!=null){
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = failure.toUserMessage())
                }
                return@launch
            }
            val todays = history.getOrDefault(emptyList())
                .filter { ride -> isToday(ride.startedAt) }

            _uiState.value = HomeUiState(
                isLoading = false,
                todaysRides = todays.filter { ride -> ride.status!= RideStatus.ACTIVE },
                totalCollected = todays.fold(BigDecimal.ZERO){
                    sum, ride -> sum+ride.collectedAmount
                },
                activeRide = active.getOrNull()
            )
        }
    }
}

class HomeViewModelFactory(
    private val rideRepository: RideRepository
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(rideRepository) as T
}