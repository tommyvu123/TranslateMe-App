package com.wordle.client.fragment

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // the text before translated
    var translateText:String=""
    // the text after translated
    var translatedText:String=""
    // from which language
    var from:String=""
    // to which language
    var to:String="ES"
    // is darkmode
    var isDarkMode: Boolean=false


}