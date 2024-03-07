package com.wordle.client.util

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

class HttpLogger: HttpLoggingInterceptor.Logger {

    /**
     * Log the request and response
     */
    override fun log(message: String) {
        Log.d("TranslateMe, Http Info:", message)
    }


}