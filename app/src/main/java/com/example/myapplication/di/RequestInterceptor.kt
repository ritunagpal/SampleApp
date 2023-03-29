package com.example.myapplication.di

import android.util.Log
import com.example.myapplication.util.Constants
import com.example.myapplication.util.Constants.API_KEY
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor : Interceptor {
    private val Log_Tag = "News APP"
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newHttpUrl = request.url()
            .newBuilder()
            .addQueryParameter("apiKey", API_KEY).build()

        val newRequest = request.newBuilder()
            .url(newHttpUrl)
            .build()

        log("OUTGOING REQ ==> $newRequest")
        val response = chain.proceed(newRequest)
        log("INCOMING RES ==> $response")
        return response
    }

    private fun log(message: String) {
        Log.d(Log_Tag, message)
    }

}
