package com.pm.ridepay

import android.app.Application
import com.pm.ridepay.data.local.ImageSaver
import com.pm.ridepay.data.local.TokenStore
import com.pm.ridepay.data.remote.ApiClient
import com.pm.ridepay.data.remote.RidePayApi
import com.pm.ridepay.data.repository.AuthRepository
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.data.repository.RideRepository
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

class RidePayApp: Application() {

    lateinit var tokenStore: TokenStore
        private set
    lateinit var api: RidePayApi
        private set
    lateinit var authRepository: AuthRepository
        private set

    lateinit var driverRepository: DriverRepository
        private set

    lateinit var imageSaver: ImageSaver
        private set

    lateinit var rideRepository: RideRepository
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore= TokenStore(this)
        api= ApiClient.create(tokenStore)
        authRepository= AuthRepository(api, tokenStore)
        driverRepository = DriverRepository(api)
        imageSaver= ImageSaver(this)
        rideRepository = RideRepository(api)
    }
}