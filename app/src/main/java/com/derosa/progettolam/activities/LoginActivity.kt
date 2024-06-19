package com.derosa.progettolam.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.pojo.User
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            val intent = Intent(this, AppActivity::class.java)
            intent.putExtra("token", "Bearer " + it.client_secret)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }

        userViewModel.observeUserCorrectlySignedUpTokenErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

}