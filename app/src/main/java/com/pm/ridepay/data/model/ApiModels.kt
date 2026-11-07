package com.pm.ridepay.data.model

import java.math.BigDecimal


enum class VerificationStatus{
    PENDING,
    VERIFIED,
    REJECTED
}

enum class RideStatus{
    ACTIVE,
    COMPLETED,
    CANCELLED
}


// Requests (android app ---> backend)


data class FirebaseLoginRequest(val firebaseIdToken: String)



data class OnboardRequest(
    val name: String,
    val bankAccountNumber: String,
    val isfcCode:String,
    val vehicleNumber: String?
)


data class StartRideRequest(
    val farePerPerson: BigDecimal,
    val passengerCount: Int
)


data class AdjustRideRequest(
    val manualAdjustment: Int?=null,
    val assignPaymentId: Long?=null
)



// Responses (backend ----> android app)


data class AuthResponse(val token: String, val driverId: Long)

data class DriverStatusResponse(
    val driverId: Long,
    val name: String?,
    val verificationStatus: VerificationStatus,
    val upiId: String?,
    val qrImageUrl: String?,
    val isActive: Boolean
)

data class PaymentDto(
    val id: Long,
    val amount: BigDecimal,
    val payerUpiId: String?,
    val receivedAt: String
)


data class RideStatusResponse(
    val rideId: Long,
    val targetAmount: BigDecimal,
    val collectedAmount: BigDecimal,
    val estimatedPaid: Int,
    val passengerCount : Int,
    val status: RideStatus,
    val payments: List<PaymentDto>
)

data class RideHistoryItem(
    val rideId: Long,
    val targetAmount: BigDecimal,
    val collectedAmount: BigDecimal,
    val status: RideStatus,
    val startedAt: String
)


data class ApiError(
    val status: Int?,
    val message: String?,
    val errors: Map<String, String>?
)