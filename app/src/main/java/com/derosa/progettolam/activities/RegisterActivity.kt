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
import com.derosa.progettolam.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val textUsername = findViewById<EditText>(R.id.textUsernameRegister)
        val textPassword = findViewById<EditText>(R.id.textPasswordRegister)
        val btnLogin = findViewById<Button>(R.id.btnRegister)
        val textLogin = findViewById<TextView>(R.id.textViewLoginLink)

        btnLogin.setOnClickListener {
            val username = textUsername.text.toString().trim()
            val password = textPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                //userViewModel.auth(User(username, password))
                userViewModel.authFake(User(username, password))
            } else {
                Toast.makeText(this, "Inserisci Username e Password", Toast.LENGTH_SHORT).show()
            }
        }

        textLogin.setOnClickListener {
            goToLogin()
        }

        userViewModel.observeUserCorrectlySignedUpLiveData().observe(this) {
            Toast.makeText(this, it.username + "" + it.id, Toast.LENGTH_SHORT).show()
            goToLogin()
        }

        userViewModel.observeUserCorrectlySignedUpErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}