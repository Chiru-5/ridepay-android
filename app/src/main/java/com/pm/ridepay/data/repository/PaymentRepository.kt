package com.pm.ridepay.data.repository

import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.data.remote.RidePayApi

class PaymentRepository(private val api: RidePayApi) {

    suspend fun getUnmatched(): Result<List<PaymentDto>> =
        safeApiCall { api.getUnmatchedPayments() }

    suspend fun ignore(paymentId: Long): Result<PaymentDto> =
        safeApiCall { api.ignorePayment(paymentId) }
}