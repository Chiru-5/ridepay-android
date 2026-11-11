package com.pm.ridepay.data.remote

import com.pm.ridepay.data.local.TokenStore
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(private val tokenStore: TokenStore): Interceptor {
    private val apiHost = ApiConfig.BASE_URL.toHttpUrl().host

    override fun intercept(chain: Interceptor.Chain): Response {
        val original=chain.request()
        val token=tokenStore.getToken()
        val request=if(token!=null && original.url.host==apiHost){
            original.newBuilder()
                .header("Authorization", "Bearer ${token}")
                .build()
        }else{
            original
        }
        return chain.proceed(request)
    }
}