package com.pm.ridepay.data.remote

import com.pm.ridepay.data.local.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.pm.ridepay.BuildConfig

object ApiConfig{
    const val BASE_URL="https://obedience-pushcart-extending.ngrok-free.dev"
}


object ApiClient {
    fun create(tokenStore: TokenStore): RidePayApi{
        val logging= HttpLoggingInterceptor().apply {
            level=if (BuildConfig.DEBUG){
                HttpLoggingInterceptor.Level.BODY
            }
            else{
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }


        val okHttp= OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logging)
            .build()


        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RidePayApi::class.java)
    }
}