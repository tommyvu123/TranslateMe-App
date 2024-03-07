package com.wordle.client.entity

import org.intellij.lang.annotations.Language

/**
 * The response body is :
 *
 * {
    "data": {
        "translations": [{"translatedText": "¡Hola Mundo!"}]
        }
    }
 */
class Languages {

    private lateinit var language: String

    private lateinit var name: String

    private var supports_formality: Boolean = false


    fun getLanguage():String{
        return language
    }

    fun getName():String{
        return name
    }

    fun getSupportsFormality():Boolean{
        return supports_formality
    }

    fun setLanguage(language: String){
        this.language = language
    }

    fun setName(name: String){
        this.name = name
    }

    fun setSupportsFormality(supports_formality: Boolean){
        this.supports_formality = supports_formality
    }
}