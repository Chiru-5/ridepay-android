package com.pm.ridepay.data.repository

import android.R.attr.path
import com.pm.ridepay.data.model.DeviceTokenRequest
import com.pm.ridepay.data.model.DriverProfileResponse
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.OnboardRequest
import com.pm.ridepay.data.remote.RidePayApi

class DriverRepository(private val api: RidePayApi) {

    suspend fun getStatus(): Result<DriverStatusResponse> = safeApiCall {
        api.getDriverStatus()
    }

    suspend fun onboard(request: OnboardRequest): Result<DriverStatusResponse> = safeApiCall {
        api.onboard(request)
    }

    suspend fun loadQrImage(path: String): Result<ByteArray> =
        safeApiCall { api.downloadFile(path).use { body -> body.bytes() } }


    suspend fun getProfile(): Result<DriverProfileResponse> =
        safeApiCall { api.getProfile() }


    suspend fun registerDriverToken(token: String): Result<Unit> =
        safeApiCall { api.registerDeviceToken(DeviceTokenRequest(token)) }
}