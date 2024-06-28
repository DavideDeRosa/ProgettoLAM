package com.derosa.progettolam.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.SharedPrefUtil
import com.derosa.progettolam.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val (savedToken, savedUsername) = SharedPrefUtil.getTokenAndUsername(this)
        if (savedToken != null && savedUsername != null) {
            DataSingleton.token = savedToken
            DataSingleton.username = savedUsername

            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

            val textUsername = findViewById<EditText>(R.id.textUsername)
            val textPassword = findViewById<EditText>(R.id.textPassword)
            val btnLogin = findViewById<Button>(R.id.btnLogin)
            val textRegister = findViewById<TextView>(R.id.textViewRegisterLink)
            lateinit var username: String

            btnLogin.setOnClickListener {
                username = textUsername.text.toString().trim()
                val password = textPassword.text.toString().trim()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    userViewModel.authToken(username, password)
                } else {
                    Toast.makeText(this, "Inserisci Username e Password", Toast.LENGTH_SHORT).show()
                }
            }

            textRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }

            userViewModel.observeUserCorrectlySignedUpTokenLiveData().observe(this) {
                DataSingleton.token = "Bearer " + it.client_secret
                DataSingleton.username = username

                SharedPrefUtil.saveTokenAndUsername("Bearer " + it.client_secret, username, this)

                val intent = Intent(this, AppActivity::class.java)
                startActivity(intent)
                finish()
            }

            userViewModel.observeUserCorrectlySignedUpTokenErrorLiveData().observe(this) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}