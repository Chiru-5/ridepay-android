package com.pm.ridepay.push

import com.google.firebase.messaging.FirebaseMessaging
import com.pm.ridepay.data.local.TokenStore
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.util.awaitResult
import kotlinx.coroutines.CancellationException

class PushRegistrar(
    private val driverRepository: DriverRepository,
    private val tokenStore: TokenStore
) {


    suspend fun syncToken(){
        if(tokenStore.getToken() == null){
            return // If there is no login then exit immediately
        }
        val fcmToken = try {
            // Requests firebase for the phone's unique device push token
            FirebaseMessaging.getInstance().token.awaitResult()
        }
        catch (e: CancellationException){
            throw  e
        }
        catch (e: Exception){
            return
        }
        // Sends the retrieved FCM token to the backend via DriverRepository. To link the device token to the logged-in user
        driverRepository.registerDriverToken(fcmToken)
    }
}