package com.derosa.progettolam.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.derosa.progettolam.R
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppActivity : AppCompatActivity() {

    val audioViewModel by lazy {
        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        //Creazione e gestione della bottomNavigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        NavigationUI.setupWithNavController(bottomNavigation, navController)
    }
}