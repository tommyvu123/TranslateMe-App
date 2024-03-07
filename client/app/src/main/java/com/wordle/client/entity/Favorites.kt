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
class Favorites(from:String, to:String, from_text:String, to_Text:String) {



    private lateinit var from: String

    private lateinit var to: String

    private lateinit var from_text: String

    private lateinit var to_text: String

    fun getFrom():String{
        return from
    }

    fun getTo():String{
        return to
    }

    fun getFromText():String{
        return from_text
    }

    fun getToText():String{
        return to_text
    }

    init{
        this.from = from
        this.to = to
        this.from_text = from_text
        this.to_text = to_Text
    }

}