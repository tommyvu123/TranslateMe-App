package com.wordle.client.util

import com.wordle.client.interfaces.TranslateService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Free language translator api
    const val DEEPL_TRANSLATE_BASE_URL = "https://api-free.deepl.com/"
    // OKhttp logging setting
    val loggingInterceptor = HttpLoggingInterceptor(HttpLogger()).setLevel(HttpLoggingInterceptor.Level.BODY)
    // OKhttp client register with logging
    val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    // Retrofit client with ok http
    val retrofit = Retrofit.Builder().baseUrl(DEEPL_TRANSLATE_BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(
        okHttpClient).build()
    // Translate service created by retrofit client
    val translateService = retrofit.create(TranslateService::class.java)

}