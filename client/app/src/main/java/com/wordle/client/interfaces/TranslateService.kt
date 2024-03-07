package com.wordle.client.interfaces

import com.wordle.client.entity.Languages
import com.wordle.client.entity.Translation
import retrofit2.Call
import retrofit2.http.*

/**
 * Deeply translate service
 * translate: Post
 * fetch supported languages: Get
 */
interface TranslateService {

    @FormUrlEncoded
    @Headers("Authorization: DeepL-Auth-Key 07db4d6f-087c-fd9e-4432-39527b7ba521:fx")
    @POST("v2/translate")
    fun translate(@Field("text") q:String, @Field("source_lang") source_lang:String, @Field("target_lang") target_lang:String): Call<Translation>

    @Headers("Authorization: DeepL-Auth-Key 07db4d6f-087c-fd9e-4432-39527b7ba521:fx")
    @GET("v2/languages?type=target")
    fun languages(): Call<List<Languages>>
}