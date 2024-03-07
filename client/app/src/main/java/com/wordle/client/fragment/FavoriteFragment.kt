package com.wordle.client.fragment

import android.content.Context
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wordle.client.R
import com.wordle.client.databinding.FragmentFavoriteBinding
import com.wordle.client.databinding.FragmentHomeBinding
import com.wordle.client.entity.Favorites
import com.wordle.client.entity.Languages
import com.wordle.client.util.LocalDBHelper
import java.lang.Exception

class FavoriteFragment : Fragment() {

    companion object {
        fun newInstance() = FavoriteFragment()
    }


    private val viewModel: FavoriteViewModel by activityViewModels()

    // Using binding to init all layout elements
    private lateinit var binding: FragmentFavoriteBinding

    // Local db util helper
    private var dbHelper: LocalDBHelper? =null
    // Local db object
    private var db:SQLiteDatabase? = null

    private val TAG = FavoriteFragment::class.java.name


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Using binding to do the findViewbyId things
        binding = FragmentFavoriteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        // Initialize the local db helper
        dbHelper = LocalDBHelper(context)

        var layoutManager:RecyclerView.LayoutManager?= null
        // Dynamically choose layout based on the orientation of device
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            layoutManager = GridLayoutManager(context,3)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
        } else  if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            layoutManager = LinearLayoutManager(context)
        }

        binding.listview.layoutManager = layoutManager
        binding.listview.adapter = FavoritesRecyclearViewAdapter(context, loadFavoritesLocally())
    }

    /**
     * Load languages from the local database
     */
    fun loadFavoritesLocally():MutableList<Favorites>{
        var favorites:MutableList<Favorites> = mutableListOf()
        // find all the languages like "select * from languages" statement
        var cursor = getDB()?.query("favorites",null,null,null,null,null,null)
        if(cursor!!.moveToFirst()){
            do{
                var fav = Favorites(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4))
                favorites.add(fav)
            } while(cursor.moveToNext())
        }
        return favorites
    }

    inner class FavoritesRecyclearViewAdapter(val context: Context?, var data:MutableList<Favorites>):RecyclerView.Adapter<FavoritesRecyclearViewAdapter.ViewHolder>(){

        inner class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
            val from:TextView = view.findViewById(R.id.from_language)
            val to:TextView = view.findViewById(R.id.to_language)
            val fromText:TextView = view.findViewById(R.id.from_text_language)
            val toText:TextView = view.findViewById(R.id.to_text_language)
            val deleteButton:Button = view.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesRecyclearViewAdapter.ViewHolder {
            val view  = LayoutInflater.from(context).inflate(R.layout.listview_item_favorite, parent, false)
            return ViewHolder(view)
        }


        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(vh: ViewHolder, position: Int) {

            var favorite:Favorites = data.get(position)

            vh.from.text = favorite.getFrom()
            vh.to.text = favorite.getTo()
            vh.fromText.text = favorite.getFromText()
            vh.toText.text = favorite.getToText()
            vh.deleteButton.setOnClickListener{
                try {
                    if(removeFromDB(favorite)) {
                        Log.d(TAG, "Delete item succeesfully!")
                        data.remove(favorite)
                        binding.listview.adapter = FavoritesRecyclearViewAdapter(context, data)
                    } else{
                        Log.d(TAG, "Delete item failed!")
                    }
                } catch (e:Exception){
                    Log.d(TAG, "Delete item failed!")
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    /**
     * Delete a favorite item from local database
     */
    fun removeFromDB(favorites: Favorites):Boolean{
        return getDB()?.delete("favorites", "from_text=? and to_text=?", arrayOf(favorites.getFromText(), favorites.getToText()))!! >0
    }


    /**
     * Get the local database object
     */
    fun getDB(): SQLiteDatabase?{
        if (db==null){
            try {
                db = dbHelper?.writableDatabase
                Log.d(TAG, "SQLite write mode")
            } catch (e: Exception){
                db = dbHelper?.readableDatabase
                Log.d(TAG, "SQLite read mode")
            }
        }
        return db
    }

}