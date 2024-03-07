package com.wordle.client.entity

class Translations {

    private lateinit var detected_source_language: String

    private lateinit var text:String

    fun getDetectedSourceLanguage():String{
        return detected_source_language
    }

    fun getText():String{
        return text
    }
}