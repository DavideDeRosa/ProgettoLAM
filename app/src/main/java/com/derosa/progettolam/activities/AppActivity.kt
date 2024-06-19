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
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        //Creazione e gestione della bottomNavigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        NavigationUI.setupWithNavController(bottomNavigation, navController)

        //Set di username e token
        token = intent.getStringExtra("token").toString()
        username = intent.getStringExtra("username").toString()

        if (token != null) {
            Log.d("token", token)

            DataSingleton.token = token
            DataSingleton.username = username
        } else {
            Log.d("token", "Token is null")
        }
    }
}