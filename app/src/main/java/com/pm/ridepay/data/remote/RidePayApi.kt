package com.pm.ridepay.data.remote

import com.pm.ridepay.data.model.AdjustRideRequest
import com.pm.ridepay.data.model.AuthResponse
import com.pm.ridepay.data.model.DeviceTokenRequest
import com.pm.ridepay.data.model.DriverProfileResponse
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.FirebaseLoginRequest
import com.pm.ridepay.data.model.OnboardRequest
import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.data.model.StartRideRequest
import com.pm.ridepay.data.repository.safeApiCall
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface RidePayApi {

    @POST("auth/firebase-login")
    suspend fun firebaseLogin(@Body request: FirebaseLoginRequest): AuthResponse


    @POST("drivers/onboard")
    suspend fun onboard(@Body request: OnboardRequest): DriverStatusResponse


    @GET("drivers/status")
    suspend fun getDriverStatus(): DriverStatusResponse

    @POST("rides/start")
    suspend fun startRide(@Body request: StartRideRequest): RideStatusResponse


//    Backend returns 204 (empty body) when there is no active ride
    @GET("rides/active")
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

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody

    @POST("payments/{paymentId}/ignore")
    suspend fun ignorePayment(@Path("paymentId") paymentId: Long): PaymentDto


    @GET("drivers/me")
    suspend fun getProfile(): DriverProfileResponse


    @PUT("drivers/me/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest)
}