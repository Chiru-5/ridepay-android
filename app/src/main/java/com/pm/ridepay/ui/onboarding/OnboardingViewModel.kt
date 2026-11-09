package com.pm.ridepay.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.OnboardRequest
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class OnboardingUiState(
    val name: String = "",
    val bankAccountNumber: String = "",
    val ifscCode: String = "",
    val vehicleNumber: String = "",
    val nameError: String? = null,
    val accountError: String? = null,
    val ifscError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val completedDriver: DriverStatusResponse? = null
)


private val ACCOUNT_REGEX = Regex("^\\d{9,18}$")
private val IFSC_REGEX= Regex("^[A-Z]{4}0[A-Z0-9]{6}$")

class OnboardingViewModel(
    private val driverRepository: DriverRepository
): ViewModel() {

    private val _uiState= MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()


    fun onNameChange(value: String) = _uiState.update {
        it.copy(name=value.take(100), nameError = null, errorMessage = null)
    }

    fun onAccountChange(value: String) = _uiState.update {
        it.copy(
            bankAccountNumber = value.filter{
                c-> c.isDigit()
            }.take(18),
            accountError = null,
            errorMessage = null
        )
    }

    fun onIfscChange(value: String) = _uiState.update {
        it.copy(
            ifscCode = value.uppercase().filter { c-> c.isLetterOrDigit() }.take(11),
            ifscError=null,
            errorMessage = null
        )
    }

    fun onVehicleChange(value: String) = _uiState.update {
        it.copy(
            vehicleNumber = value.uppercase().filter { c-> c.isLetterOrDigit() }.take(20),
            errorMessage = null
        )
    }


    fun submit(){
        val s=_uiState.value
        if(s.isSubmitting){
            return
        }

        val nameError=if(s.name.trim().length<2) "Enter your full name" else null
        val accountError=if(!ACCOUNT_REGEX.matches(s.bankAccountNumber)) "Enter 9 to 18 digits" else null
        val ifscError=if(!IFSC_REGEX.matches(s.ifscCode)) "Enter a valid IFSC, like SBIN0001234" else null

        if(nameError!=null || accountError!=null || ifscError!=null){
            _uiState.update {
                it.copy(nameError=nameError, accountError = accountError, ifscError = ifscError)
            }
            return
        }

        _uiState.update{
            it.copy(isSubmitting = true, errorMessage = null)
        }
        viewModelScope.launch {
            val request = OnboardRequest(
                name = s.name.trim(),
                bankAccountNumber = s.bankAccountNumber,
                ifscCode = s.ifscCode,
                vehicleNumber = s.vehicleNumber.ifBlank { null }
            )

            driverRepository.onboard(request)
                .onSuccess {
                    driver -> _uiState.update {
                        it.copy(isSubmitting = false, completedDriver = driver)
                }
                }.onFailure { error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = error.toUserMessage())
                } }
        }
    }
}

class OnboardingViewModelFactory(
    private val driverRepository: DriverRepository
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T= OnboardingViewModel(driverRepository) as T
}