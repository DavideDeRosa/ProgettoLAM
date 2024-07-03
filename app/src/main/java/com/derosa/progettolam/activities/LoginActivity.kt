package com.derosa.progettolam.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.notification.ConnectivityService
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        requestNotificationPermission()
        startService(Intent(this, ConnectivityService::class.java))

        val isNetworkAvailable = ExtraUtil.isNetworkAvailable(this)
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("network_state", isNetworkAvailable)
            apply()
        }

        if (!isNetworkAvailable) {
            Toast.makeText(
                this,
                "Nessuna connessione.\nSei in modalità Offline!",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val (savedToken, savedUsername) = ExtraUtil.getTokenAndUsername(this)
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
                        Toast.makeText(this, "Inserisci Username e Password", Toast.LENGTH_SHORT)
                            .show()
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

                    ExtraUtil.saveTokenAndUsername("Bearer " + it.client_secret, username, this)

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

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(
                    this,
                    "Rifiutando le notifiche non potrai ricevere aggiornamenti!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    }
}