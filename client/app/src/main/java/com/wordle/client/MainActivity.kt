package com.wordle.client

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wordle.client.fragment.*

class MainActivity : AppCompatActivity() {

    lateinit var demoButton: Button

    var mContext:Context = this

//    var g:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        initView()
    }

    private lateinit var mBottomNavigationView: BottomNavigationView

    private fun loadFragment(fragment: Fragment){


        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)

        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initView(){
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view)

        mBottomNavigationView.setOnItemSelectedListener{
            when (it.itemId){
                R.id.home ->{
                    setTitle(R.string.home)
                    loadFragment(HomeFragment())

                    return@setOnItemSelectedListener true
                }
                R.id.favorite ->{
                    setTitle(R.string.favorite)
                    loadFragment(FavoriteFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.currency ->{
                    setTitle(R.string.currency)
                    loadFragment(CurrencyFragment())
                    return@setOnItemSelectedListener true

                }

            }
            false

        }
    }
}

