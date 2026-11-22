package com.pm.ridepay.ui.startride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.StartRideRequest
import com.pm.ridepay.data.repository.RideRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal


private const val MAX_PASSENGERS = 20
private val MIN_FARE= BigDecimal.ONE


data class StartRideUiState(
    val fareText: String="",
    val passengersText: String="",
    val fareError: String?=null,
    val passengersError: String?=null,
    val isSubmitting: Boolean=false,
    val errorMessage: String?=null,
    val startedRideId: Long?=null
){
    val fare: BigDecimal? get() = fareText.toBigDecimalOrNull()
    val passengers: Int? get() = passengersText.toIntOrNull()

    val total: BigDecimal?
        get(){
            val f=fare
            val p=passengers
            return if(f!=null && p!=null && p>0) f.multiply(BigDecimal(p)) else null
        }
}

class StartRideViewModel(
    private val rideRepository: RideRepository,
    private val assignPaymentId: Long?
): ViewModel() {

    private val _uiState= MutableStateFlow(StartRideUiState())
    val uiState: StateFlow<StartRideUiState> = _uiState.asStateFlow()

    fun onFareChange(value: String) = _uiState.update {
        it.copy(fareText = cleanFare(value), fareError=null, errorMessage = null)
    }

    fun onPassengersChange(value: String) = _uiState.update{
        it.copy(
            passengersText = value.filter { c->c.isDigit()}.take(2),
            passengersError = null,
            errorMessage = null
        )
    }

    fun start(){
        val s = _uiState.value
        if(s.isSubmitting) return

        val fare=s.fare
        val passengers = s.passengers
        val fareError = if(fare==null || fare<MIN_FARE) "Enter a fare of 1 or more" else null
        val passengersError=if(passengers==null || passengers !in 1..MAX_PASSENGERS){
            "Enter 1 to ${MAX_PASSENGERS} passengers"
        }
        else{
            null
        }

        if(fareError!=null || passengersError!=null || fare==null || passengers==null){
            _uiState.update {
                it.copy(fareError = fareError, passengersError = passengersError)
            }
            return
        }


        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = rideRepository.startRide(StartRideRequest(fare, passengers))
            val ride = result.getOrNull()
            if (ride == null) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.toUserMessage()
                            ?: "Could not start the ride."
                    )
                }
                return@launch
            }

            // If we came from the unassigned list, attach that payment to the new ride.
            // If this fails, the payment simply stays in the list.
            if (assignPaymentId != null) {
                rideRepository.assignPayment(ride.rideId, assignPaymentId)
            }

            _uiState.update { it.copy(isSubmitting = false, startedRideId = ride.rideId) }
        }
    }
}

private fun cleanFare(input: String): String{
    val filtered = input.filter { it.isDigit() || it=='.' }
    val dot=filtered.indexOf('.')
    if(dot==-1) return filtered.take(5)
    val whole=filtered.substring(0, dot).take(5)
    val decimals=filtered.substring(dot+1).filter { it.isDigit() }.take(2)
    return "$whole.$decimals"
}

class StartRideViewModelFactory(
    private val rideRepository: RideRepository,
    private val assignPaymentId: Long?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        StartRideViewModel(rideRepository, assignPaymentId) as T
}