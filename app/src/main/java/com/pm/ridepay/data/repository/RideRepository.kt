package com.pm.ridepay.data.repository


import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.model.StartRideRequest
import com.pm.ridepay.data.remote.RidePayApi
import retrofit2.HttpException

class RideRepository(private val api: RidePayApi) {

    suspend fun getHistory(): Result<List<RideHistoryItem>> = safeApiCall {
        api.getHistory()
    }

    suspend fun startRide(request: StartRideRequest): Result<RideStatusResponse> =
        safeApiCall { api.startRide(request) }


    suspend fun getRideStatus(rideId: Long): Result<RideStatusResponse> =
        safeApiCall{api.getRideStatus(rideId)}

    suspend fun cancelRide(rideId: Long): Result<RideStatusResponse> =
        safeApiCall { api.cancelRide(rideId) }


//    If there is no ride at present backend will answers 204(no body)
    suspend fun getActiveRide(): Result<RideStatusResponse?> = safeApiCall {
        val response=api.getActiveRide()
        when {
            response.code() == 204 -> null
            response.isSuccessful -> response.body()
            else -> throw HttpException(response)
        }
    }
}