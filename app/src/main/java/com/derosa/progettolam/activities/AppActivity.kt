package com.derosa.progettolam.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.derosa.progettolam.R
import com.derosa.progettolam.util.DataSingleton
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppActivity : AppCompatActivity() {

    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        //Creazione e gestione della bottomNavigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        NavigationUI.setupWithNavController(bottomNavigation, navController)

        //Set dell'user token
        token = intent.getStringExtra("token").toString()

        if (token != null) {
            Log.d("token", token)

            DataSingleton.token = token
        } else {
            Log.d("token", "Token is null")
        }
    }
}