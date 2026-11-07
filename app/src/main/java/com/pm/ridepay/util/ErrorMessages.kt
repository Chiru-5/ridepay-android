package com.pm.ridepay.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.gson.Gson
import com.pm.ridepay.data.model.ApiError
import okio.IOException
import retrofit2.HttpException

fun Throwable.toUserMessage(): String =when (this){
    is HttpException ->{
        val body=response()?.errorBody()?.string()
        val parsed=try{
            Gson().fromJson(body, ApiError::class.java)
        }
        catch (e: Exception){
            null
        }
        parsed?.message
            ?:parsed?.errors?.values?.firstOrNull()
            ?:"Server error (${code()}"
    }

    is IOException -> "Cannot reach the server. Check your internet connection."
    is FirebaseNetworkException -> "No internet connection."
    is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
    is FirebaseAuthInvalidCredentialsException -> "Invalid phone number or code."
    else -> "Something wnt wrong. Please try again."
}