package com.derosa.progettolam.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.derosa.progettolam.R
import com.derosa.progettolam.adapters.SpacingItemDecoration
import com.derosa.progettolam.adapters.UploadAdapter
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUpload)
        val uploadAdapter = UploadAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = uploadAdapter
        recyclerView.addItemDecoration(SpacingItemDecoration(20))

        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]

        val username = DataSingleton.username
        if (username != null) {
            audioViewModel.getAllUploadByUsernameDb(username)
        }

        audioViewModel.observeListUploadDbLiveData().observe(this) {
            uploadAdapter.setUploadList(ArrayList(it))
        }

        uploadAdapter.onItemClick = {
            if (ExtraUtil.isWifiConnected(this)) {
                uploadAudio(it.username!!, it.longitude!!, it.latitude!!)
            } else {
                showUploadConfirmationDialog(it.username!!, it.longitude!!, it.latitude!!)
            }
        }
    }

    private fun showUploadConfirmationDialog(
        username: String,
        longitude: Double,
        latitude: Double
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma")
            .setMessage("Non sei connesso ad una rete Wi-Fi. Vuoi continuare il caricamento con i dati mobili?")
            .setPositiveButton("Si") { dialog, which ->
                uploadAudio(username, longitude, latitude)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadAudio(username: String, longitude: Double, latitude: Double) {
        val token = DataSingleton.token
        if (token != null) {
            val mp3FilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp3"
            val mp3File = File(mp3FilePath)

            if (mp3File.exists() && mp3File.canRead()) {
                val requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), mp3File)
                val fileUpload =
                    MultipartBody.Part.createFormData("file", mp3File.name, requestBody)

                audioViewModel.uploadAudio(token, longitude, latitude, fileUpload)

                audioViewModel.observeFileCorrectlyUploadedLiveData().observe(this) {
                    audioViewModel.deleteUploadDb(username, longitude, latitude)
                    Toast.makeText(this, "Audio caricato correttamente!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("File Error", "MP3 file does not exist or cannot be read.")

                Toast.makeText(this, "C'è stato un errore!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToAppActivity() {
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToAppActivity()
    }
}