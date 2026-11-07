package com.pm.ridepay.ui.login;


import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.pm.ridepay.data.repository.AuthRepository;
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

import kotlin.text.Regex;

enum class LoginStep { PHONE, OTP }


data class LoginUiState(
    val step: LoginStep=LoginStep.PHONE,
    val phone: String="",
    val otp: String="",
    val isLoading: Boolean=false,
    val errorMessage: String?=null,
    val loggedIn: Boolean=false

)

private val PHONE_REGEX = Regex("^[6-9]\\d{9}$")
public class LoginViewModel(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth=FirebaseAuth.getInstance()
): ViewModel() {
    private val _uiState= MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var verificationId: String? =null

    fun onPhoneChange(value: String){
        val digits=value.filter {
            it.isDigit()
        }.take(10)
        _uiState.update {
            it.copy(phone=digits, errorMessage = null)
        }
    }

    fun onOtpChange(value: String){
        val digits=value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otp = digits, errorMessage = null) }
    }

    fun sendOtp(activity: Activity){
        if(_uiState.value.isLoading){
            return
        }
        val phone=_uiState.value.phone
        if(!PHONE_REGEX.matches(phone)){
            fail("Enter a  valid 10-digit mobile number")
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val callbacks=object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                fail(e.toUserMessage())
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId=id
                _uiState.update { it.copy(isLoading = false, step = LoginStep.OTP) }
            }
        }

        val options= PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    fun verifyOtp(){
        if(_uiState.value.isLoading) return
        val id = verificationId?: return
        val code=_uiState.value.otp
        if(code.length!=6){
            fail("Enter the 6-digit code")
            return
        }
        signInWithCredential(PhoneAuthProvider.getCredential(id, code))
    }

    fun backToPhone(){
        verificationId=null
        _uiState.update {
            it.copy(step = LoginStep.PHONE, otp = "", errorMessage = null)
        }
    }

    fun reset(){
        verificationId=null
        _uiState.value= LoginUiState()
    }

    private fun signInWithCredential(credential: PhoneAuthCredential){
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user=result.user
                if(user==null){
                    fail("Login failed. Please try again")
                    return@addOnSuccessListener
                }
                user.getIdToken(false)
                    .addOnSuccessListener { tokenResult ->
                        val idToken=tokenResult.token
                        if(idToken==null){
                            fail("Login failed. Please try again.")
                        }
                        else{
                            loginToBackend(idToken)
                        }
                    }
                    .addOnFailureListener { error -> fail(error.toUserMessage()) }
            }
            .addOnFailureListener { error-> fail(error.toUserMessage()) }
    }

    private fun loginToBackend(idToken: String){
        viewModelScope.launch{
            authRepository.loginWithFirebaseToken(idToken)
                .onSuccess { _uiState.update { state -> state.copy(isLoading = false, loggedIn = true) }
                }
                .onFailure { error-> fail(error.toUserMessage()) }
        }
    }

    private fun fail(message: String){
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}


class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(authRepository) as T
}




