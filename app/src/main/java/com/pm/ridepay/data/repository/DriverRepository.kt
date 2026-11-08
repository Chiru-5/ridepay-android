package com.pm.ridepay.data.repository

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
}