package com.wordle.client.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wordle.client.R
import com.wordle.client.entity.Languages

open class BottomDialogSheetFragment(isFrom:Boolean, homeFragment: HomeFragment): SuperBottomSheetFragment() {

    var isFrom:Boolean = false
    var homeFragment: HomeFragment

    init{
        this.isFrom = isFrom
        this.homeFragment = homeFragment
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // the layout for the dialog
        var root = layoutInflater.inflate(R.layout.dialog_show_from_bottom, null)
        // the listview for the dialog
        var listView: ListView = root.findViewById(R.id.listview)
        var title: TextView = root.findViewById(R.id.title)
        var detectButton: Button = root.findViewById(R.id.detect_language)
        var searchView:androidx.appcompat.widget.SearchView = root.findViewById(R.id.searchview)

        var languageList:MutableList<Languages> = homeFragment.loadLanguagesLocally()
        // listview adapter
        listView.adapter = HomeFragment.LanguagesAdapter(context, languageList)
        // listview item click listener
        listView.setOnItemClickListener(languagesItemClickListener)

        if(isFrom){
            listView.tag=homeFragment.TAG_FROM_LANGUAGE
            title.setText(R.string.translate_from)
        } else {
            listView.tag=homeFragment.TAG_TO_LANGUAGE
            title.setText(R.string.translate_to)
            detectButton.visibility = View.INVISIBLE
        }

        detectButton.setOnClickListener {
            // only supports from original text
            homeFragment.binding.fromLanguage.setText(R.string.detect_language)
            homeFragment.viewModel.from = ""
            dismiss()
        }

        searchView.setOnQueryTextListener(object:
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                val filterList = mutableListOf<Languages>()
                languageList.forEach {
                    if(it.getName().contains(p0!!)){
                        filterList.add(it)
                    }
                }
                listView.adapter = HomeFragment.LanguagesAdapter(context, filterList)

                return false
            }

        })

        return root
    }

    /**
     * The languages list item click listener
     */
    var languagesItemClickListener: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener{ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->

        if(adapterView.tag!=null) {
            // get which language the user click
            var language: Languages = adapterView.getItemAtPosition(i) as Languages
            if(adapterView.tag == homeFragment.TAG_FROM_LANGUAGE){
                // update the text of the language
                homeFragment.binding.fromLanguage.text = language.getName()
                // set the abbreviation for the language, this will be send to the
                homeFragment.viewModel.from = language.getLanguage()
            } else if (adapterView.tag == homeFragment.TAG_TO_LANGUAGE){
                // update the text of the language
                homeFragment.binding.toLanguage.text = language.getName()
                // set the abbreviation for the language, this will be send to the
                homeFragment.viewModel.to = language.getLanguage()
            }

           dismiss()
        }
    }

    override fun getPeekHeight(): Int {

        super.getPeekHeight()

        with(resources.displayMetrics) {
            return heightPixels - 300
        }

    }

}