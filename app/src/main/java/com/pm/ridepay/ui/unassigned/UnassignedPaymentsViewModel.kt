package com.pm.ridepay.ui.unassigned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.data.model.RideStatus
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.repository.PaymentRepository
import com.pm.ridepay.data.repository.RideRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class UnassignedUiState(
    val isLoading: Boolean = true,
    val payments: List<PaymentDto> = emptyList(),
    val activeRide: RideStatusResponse? = null,
    val busyPaymentId: Long? = null,
    val errorMessage: String? = null,
    val message: String?=null
)




class UnassignedPaymentsViewModel(
    private val rideRepository: RideRepository,
    private val paymentRepository: PaymentRepository
): ViewModel() {
  private val _uiState = MutableStateFlow(UnassignedUiState())
  val uiState: StateFlow<UnassignedUiState> = _uiState.asStateFlow()

    fun load(){
        viewModelScope.launch {
            val unmatched = paymentRepository.getUnmatched()
            val active = rideRepository.getActiveRide()

            val failure = unmatched.exceptionOrNull() ?: active.exceptionOrNull()
            if(failure!=null){
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = failure.toUserMessage())
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    payments = unmatched.getOrDefault(emptyList()).sortedByDescending { p->p.id },
                    activeRide = active.getOrNull(),
                    errorMessage = null
                )
            }
        }
    }


    fun addToCurrentRide(paymentId: Long){
        val ride = _uiState.value.activeRide ?: return
        if(_uiState.value.busyPaymentId!=null) return
        _uiState.update { it.copy(busyPaymentId = paymentId, message = null) }

        viewModelScope.launch {
            rideRepository.assignPayment(ride.rideId, paymentId)
                .onSuccess { updated ->
                    val text = if(updated.status == RideStatus.COMPLETED){
                        "Added. The ride is now complete."
                    }
                    else{
                        "Added to the current ride."
                    }
                    _uiState.update { it.copy(busyPaymentId = null, message = text) }
                    load()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(busyPaymentId = null, message = error.toUserMessage())
                    }
                }
        }
    }

    fun ignore(paymentId: Long){
        if(_uiState.value.busyPaymentId!=null) return
        _uiState.update { it.copy(busyPaymentId = paymentId, message = null) }

        viewModelScope.launch {
            paymentRepository.ignore(paymentId)
                .onSuccess {
                    _uiState.update { it.copy(busyPaymentId = null, message = "Payment ignored.") }
                    load()
                }
                .onFailure { error -> _uiState.update { it.copy(busyPaymentId = null, message = error.toUserMessage()) } }
        }
    }
}


class UnassignedPaymentsViewModelFactory(
    private val rideRepository: RideRepository,
    private val paymentRepository: PaymentRepository
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        UnassignedPaymentsViewModel(rideRepository, paymentRepository) as T
}