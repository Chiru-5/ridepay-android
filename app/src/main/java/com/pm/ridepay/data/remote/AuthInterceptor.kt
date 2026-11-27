package com.pm.ridepay.data.remote

import com.pm.ridepay.data.local.SessionEvents
import com.pm.ridepay.data.local.TokenStore
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val sessionEvents: SessionEvents
): Interceptor {
    private val apiHost = ApiConfig.BASE_URL.toHttpUrl().host

    override fun intercept(chain: Interceptor.Chain): Response {
        val original=chain.request()
        val token=tokenStore.getToken()
        val sendToken = token!=null && original.url.host == apiHost

        val request = if(sendToken){
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        else{
            original
        }

        val response = chain.proceed(request)
        if(response.code==401 && sendToken && tokenStore.getToken() == token){
            sessionEvents.notifyExpired()
        }
        return response
    }
}