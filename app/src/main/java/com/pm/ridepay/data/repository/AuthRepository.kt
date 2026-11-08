package com.pm.ridepay.data.repository

import androidx.browser.trusted.Token
import com.google.firebase.auth.FirebaseAuth
import com.pm.ridepay.data.local.TokenStore
import com.pm.ridepay.data.model.AuthResponse
import com.pm.ridepay.data.model.FirebaseLoginRequest
import com.pm.ridepay.data.remote.RidePayApi

class AuthRepository(
    private val api: RidePayApi,
    private val tokenStore: TokenStore
) {
    suspend fun loginWithFirebaseToken(firebaseIdToken: String): Result<AuthResponse> =
        safeApiCall {
            api.firebaseLogin(FirebaseLoginRequest(firebaseIdToken))
        }.onSuccess {
            response -> tokenStore.saveToken(response.token)
        }

    fun isLoggedIn(): Boolean = tokenStore.getToken()!=null
    fun logout(){
        tokenStore.clear()
        FirebaseAuth.getInstance().signOut()
    }
}