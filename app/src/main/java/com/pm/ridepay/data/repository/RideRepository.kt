package com.pm.ridepay.data.repository

import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.remote.RidePayApi

class RideRepository(private val api: RidePayApi) {

    suspend fun getHistory(): Result<List<RideHistoryItem>> = safeApiCall {
        api.getHistory()
    }
}