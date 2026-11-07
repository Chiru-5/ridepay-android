package com.pm.ridepay.data.remote

import com.pm.ridepay.data.model.AdjustRideRequest
import com.pm.ridepay.data.model.AuthResponse
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.FirebaseLoginRequest
import com.pm.ridepay.data.model.OnboardRequest
import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.model.StartRideRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RidePayApi {

    @POST("auth/firebase-login")
    suspend fun firebaseLogin(@Body request: FirebaseLoginRequest): AuthResponse


    @POST("drivers/onboard")
    suspend fun onboard(@Body request: OnboardRequest): DriverStatusResponse


    @GET("drivers/status")
    suspend fun getDriverStatus(): DriverStatusResponse

    @POST("ride/start")
    suspend fun startRide(@Body request: StartRideRequest): RideStatusResponse


//    Backend returns 204 (empty body) when there is no active ride
    @GET("rides/history")
    suspend fun getActiveRide(): Response<RideStatusResponse>

    @GET("rides/history")
    suspend fun getHistory(): List<RideHistoryItem>

    @GET("rides/{rideId}/status")
    suspend fun getRideStatus(@Path("rideId") rideId:Long): RideStatusResponse


    @POST("rides/{rideId}/cancel")
    suspend fun cancelRide(@Path("rideId") rideId: Long): RideStatusResponse


    @POST("rides/{rideId}/adjust")
    suspend fun adjustRide(
        @Path("rideId") rideId: Long,
        @Body request: AdjustRideRequest
    ): RideStatusResponse

    @GET("payments/unmatched")
    suspend fun getUnmatchedPayments(): List<PaymentDto>
}