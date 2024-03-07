package com.wordle.client.entity

/**
 * The response body is :
 *
 * {
    "data": {
        "translations": [{"translatedText": "¡Hola Mundo!"}]
        }
    }
 */
class Translation {

    private lateinit var translations: List<Translations>

    fun getTranslations():List<Translations>{
        return translations
    }
}