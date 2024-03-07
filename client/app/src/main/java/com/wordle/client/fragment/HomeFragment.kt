package com.wordle.client.fragment

import android.app.Activity
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wordle.client.R
import com.wordle.client.databinding.FragmentHomeBinding
import com.wordle.client.entity.Favorites
import com.wordle.client.entity.Languages
import com.wordle.client.entity.Translation
import com.wordle.client.util.LocalDBHelper
import com.wordle.client.util.ProgressDialog
import com.wordle.client.util.RetrofitClient
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }
    // Using binding to init all layout elements
    lateinit var binding:FragmentHomeBinding
    // Using videModel to store data
    val viewModel: HomeViewModel by activityViewModels()

    // Local db util helper
    private var dbHelper:LocalDBHelper? =null
    // Local db object
    private var db:SQLiteDatabase? = null
    // The tag for this Fragment
    private val TAG = HomeFragment::class.java.name
    // The tag of the textview
    val TAG_FROM_LANGUAGE = 0x3
    // The tag of the textview
    val TAG_TO_LANGUAGE = 0x4

    var initListener = TextToSpeech.OnInitListener {
        if(it == TextToSpeech.SUCCESS){
            Log.d(TAG, "TTS is speaking")
        } else {
            Log.d(TAG, "TTS can't work")
        }
    }
    lateinit var tts:TextToSpeech

//    val NIGHT_MODE = "night_mode"
//
//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name=NIGHT_MODE)
//
//    object PreferenceKeys{
//        val NIGHT_MODE_KEY = stringPreferencesKey("mode_key")
//    }
//
//    private suspend fun Context.saveNightMode(mode:String){
//        dataStore.edit {
//            it[PreferenceKeys.NIGHT_MODE_KEY] = mode
//        }
//    }
//
//    suspend fun Context.getNightMode() = dataStore.data.catch {exception->
//        emit(emptyPreferences())
//    }.map {
//        val mode = it[PreferenceKeys.NIGHT_MODE_KEY]?:-1
//
//        return@map mode
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, " onCreateView")
        // Using binding to do the findViewbyId things
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // Set the onclick event for the textview(from language)
        binding.fromLanguage.setOnClickListener(buttonClickListener)
        // Set the onclick event for the textview((to language)
        binding.toLanguage.setOnClickListener(buttonClickListener)
        // set the translate button click listener
        binding.translateToButton.setOnClickListener(buttonClickListener)
        binding.markButton.setOnClickListener(buttonClickListener)
        binding.copyButton.setOnClickListener(buttonClickListener)
        binding.swapImage.setOnClickListener(buttonClickListener)
        binding.fromEdittext.setText(viewModel.translateText)
        binding.play1Button.setOnClickListener(buttonClickListener)
        binding.play2Button.setOnClickListener(buttonClickListener)

        binding.darkmodeButton.setOnCheckedChangeListener{ buttonView, isChecked->
            viewModel.isDarkMode = isChecked
            if (isChecked) {
                Log.e("Msg","Night mode button switch on")

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else {
                Log.e("Msg","Night mode button switch off")

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.fromEdittext.addTextChangedListener (
            object :TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    viewModel.translateText = p0.toString()
                    if(isBookmarkExisted()!=null){
                        binding.markButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
                    } else{
                        binding.markButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
                    }
                }

            })

        binding.translatedTextview.setText(viewModel.translatedText)

        binding.darkmodeButton.isChecked = viewModel.isDarkMode


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize the local db helper
        dbHelper = LocalDBHelper(context)
        // Load all the languages from local database
        var languages = loadLanguagesLocally()
        // if the data is empty in the local database, then fetch from restful api
        if(languages == null || languages.size<1){
            fetchLanguages()
        }
        if(isBookmarkExisted()!=null){
            binding.markButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
        }
        tts = TextToSpeech(context,initListener)


    }

    /**
     * Handle all button clicklistener in one function
     */
    private var buttonClickListener:View.OnClickListener = View.OnClickListener {
        if(it == binding.fromLanguage || it == binding.toLanguage){
           chooseLanguage(it==binding.fromLanguage)
        } else if (it == binding.translateToButton){
           translate()
        } else if (it == binding.markButton){
           bookmark()
        } else if (it == binding.copyButton) run {
            if(binding.translatedTextview.text.toString().isEmpty()){
                Toast.makeText(context, R.string.error_copy_translated_language, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var cm: ClipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var clipData:ClipData = ClipData.newPlainText("Label", binding.toLanguage.text.toString())
            cm.setPrimaryClip(clipData)
            Toast.makeText(context,R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        } else if (it==binding.swapImage){
            if(viewModel.from.equals("")){
                Toast.makeText(context,R.string.error_from_language, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            // exchange
            val tempFrom = viewModel.from
            val tempText =  binding.fromLanguage.text

            binding.fromLanguage.text = binding.toLanguage.text
            binding.toLanguage.text = tempText

            viewModel.from = viewModel.to
            viewModel.to = tempFrom
        } else if (it == binding.play2Button){
            tts.speak(binding.translatedTextview.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        } else if (it==binding.play1Button){
            tts.speak(binding.fromEdittext.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }



    /**
     * Choose a language from a pop up dialog
     */
    private fun chooseLanguage(isFrom:Boolean){
        // The bottom sheet dialog will show
        BottomDialogSheetFragment(isFrom, this).show(requireActivity().supportFragmentManager,"BottomDialogSheetFragment")
    }

    /**
     * Check if the book mark is already existed
     */
    private fun isBookmarkExisted(): Favorites? {
        var cursor = getDB()?.query("favorites",null,"from_text=? and to_text=?",  arrayOf(binding.fromEdittext.text.toString(), binding.translatedTextview.text.toString()),null,null,null)
        if(cursor!!.moveToFirst()){
            do{
                var fav = Favorites(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4))
                return fav
            } while(cursor.moveToNext())
        }
        return null
    }

    /**
     * Book mark
     */
    private fun bookmark(){
        // If the book mark is existed
        val fav = isBookmarkExisted()
        if(fav!=null){
            if(getDB()?.delete("favorites", "from_text=? and to_text=?", arrayOf(fav.getFromText(), fav.getToText()))!! >0){
                binding.markButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
                Log.d(TAG, "bookmark remove!")
            } else{
                Log.d(TAG, "bookmark remove failed!")
            }
        } else {
                val content = ContentValues().apply {
                // only save those supported language
                put("_from", binding.fromLanguage.text.toString())
                put("_to", binding.toLanguage.text.toString())
                put("from_text", binding.fromEdittext.text.toString())
                put("to_text", binding.translatedTextview.text.toString())

            }

            getDB()?.insert("favorites", null, content)
            binding.markButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
        }
    }

    private fun translate(){
        val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)

        // record the click event
        Log.i(TAG, "Translate button are clicked.")
        // get the text user input
        val text = binding.fromEdittext.text.toString()
        if (checkTranslateText(text)){
            // translate service api
            val api = RetrofitClient.translateService
            // progress dialog
            var dialog = ProgressDialog()
            // show the progress
            dialog.showProgress(context)
            var from = ""

            if(viewModel.from!=null){
                from = viewModel.from

                var fromList:List<String> = from.split("-")
                if(fromList.size>1){
                    from = fromList.get(0)
                }
            }

            // call restful api, post request.
            api.translate(text,from, viewModel.to).enqueue(object : Callback<Translation> {
                override fun onResponse(
                    call: Call<Translation>,
                    response: Response<Translation>
                ) {
                    println(response.body())
                    var translated: Translation? = response.body()
                    if (translated != null) {
                        Log.d(TAG, "Translated success, $text")
                        Log.d(
                            TAG,
                            "detected_source_language: ${translated.getTranslations()[0].getDetectedSourceLanguage()}"
                        )
                        Log.d(TAG, "text: ${translated.getTranslations()[0].getText()}")
                        binding.translatedTextview.setText(translated.getTranslations()[0].getText())
                        viewModel.translatedText = binding.translatedTextview.text.toString()

                    }
                    dialog.closeProgress()
                }

                override fun onFailure(call: Call<Translation>, t: Throwable) {
                    Log.e(TAG, "Translated failed, $text")
                    dialog.closeProgress()
                }

            })
        }
    }

    private fun checkTranslateText(text:String):Boolean{
        if (text.isEmpty()){
            Toast.makeText(context, R.string.empty_string_checking, Toast.LENGTH_SHORT).show()
        }
        return !text.isEmpty()
    }


    /**
     * Load languages from the local database
     */
    fun loadLanguagesLocally():MutableList<Languages>{
        var languages:MutableList<Languages> = mutableListOf()
        // find all the languages like "select * from languages" statement
        var cursor = getDB()?.query("languages",null,null,null,null,null,null)
        if(cursor!!.moveToFirst()){
            do{
                var lan = Languages()
                lan.setLanguage(cursor.getString(1))
                lan.setName(cursor.getString(2))
                lan.setSupportsFormality(cursor.getInt(3)>0)
                // add the languages to a mutable list
                languages?.add(lan)
            } while(cursor.moveToNext())
        }
        return languages
    }

    /**
     * Fetch all languages from the remote restful service.
     */
    fun fetchLanguages(){
        // custom progress dialog
        var dialog = ProgressDialog()
        // show the progress dialog to let the user wait
        dialog.showProgress(context)
        // translate service api
        val api = RetrofitClient.translateService
        // async run
        api.languages().enqueue(object : Callback<List<Languages>> {
            override fun onResponse(call: Call<List<Languages>>, response: Response<List<Languages>>) {
                Log.d(TAG,response.body().toString())
                if(response.isSuccessful){
                var languages: List<Languages>? = response.body()
                if (languages != null) {
                    for (language in languages){
                        val content = ContentValues().apply {
                            // only save those supported language

                                put("language", language.getLanguage())
                                put("name", language.getName())
                                put("supports_formality", language.getSupportsFormality())

                        }
                        if(content!=null)
                            getDB()?.insert("languages", null , content)
                    }
                    Log.d(TAG, "Fetched all languages")
                }
                } else{
                    Toast.makeText(context, R.string.fetch_data_fail, Toast.LENGTH_SHORT).show()
                }
                dialog.closeProgress()
            }

            override fun onFailure(call: Call<List<Languages>>, t: Throwable) {
                Log.e(TAG,"Fetch languages failed!")
                Toast.makeText(context, R.string.fetch_data_fail, Toast.LENGTH_SHORT).show()
                dialog.closeProgress()
            }

        })
    }

    /**
     * Get the local database object
     */
    fun getDB():SQLiteDatabase?{
        if (db==null){
            try {
                db = dbHelper?.writableDatabase
                Log.d(TAG, "SQLite write mode")
            } catch (e:Exception){
                db = dbHelper?.readableDatabase
                Log.d(TAG, "SQLite read mode")
            }
        }
        return db
    }

    /**
     * The languages adapter for the bottom sheet dialog
     */
    open class LanguagesAdapter(val context: Context?, var data:MutableList<Languages>): BaseAdapter() {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(p0: Int): Any {
            return data.get(p0)
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        inner class ViewHolder(v: View) {
            val name: TextView = v.findViewById(R.id.name)

        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var vh: ViewHolder? =null
            val view: View
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.listview_item_language, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
            } else{
                view = convertView
                vh = view.tag as ViewHolder
            }

            var languages:Languages = getItem(position) as Languages
            if (languages!=null){
                if (vh != null) {
                    vh.name.text = languages.getName()
                }
            }
            return view
        }
    }

}