package com.pm.ridepay

import android.app.Application
import com.pm.ridepay.data.local.TokenStore
import com.pm.ridepay.data.remote.ApiClient
import com.pm.ridepay.data.remote.RidePayApi
import com.pm.ridepay.data.repository.AuthRepository
import com.pm.ridepay.data.repository.DriverRepository

class RidePayApp: Application() {

    lateinit var tokenStore: TokenStore
        private set
    lateinit var api: RidePayApi
        private set
    lateinit var authRepository: AuthRepository
        private set

    lateinit var driverRepository: DriverRepository
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore= TokenStore(this)
        api= ApiClient.create(tokenStore)
        authRepository= AuthRepository(api, tokenStore)
        driverRepository = DriverRepository(api)
    }
}