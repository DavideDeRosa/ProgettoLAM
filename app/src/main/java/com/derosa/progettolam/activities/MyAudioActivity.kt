package com.derosa.progettolam.activities

import android.app.AlertDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.adapters.MyAudioAdapter
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.AudioViewModel
import java.io.IOException
import java.util.Locale

class MyAudioActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var myAudioAdapter: MyAudioAdapter
    private lateinit var audio: AudioMetaData
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_audio)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        myAudioAdapter = MyAudioAdapter(this)

        audioViewModel.observeAudioByIdLiveData().observe(this) {
            audio = it
            initialize()
        }

        audioViewModel.observeAudioByIdErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        val id = intent.getIntExtra("audio_id", 0)

        val token = DataSingleton.token
        if (token != null) {
            audioViewModel.getAudioById(token, id)
        }
    }

    private fun initialize() {
        findViewById<TextView>(R.id.textIniziale).visibility = View.VISIBLE

        findViewById<TextView>(R.id.textLongitude).text = "Longitudine: ${audio.longitude}"
        findViewById<TextView>(R.id.textLatitude).text = "Latitudine: ${audio.latitude}"
        findViewById<TextView>(R.id.textCreatorUsername).text =
            "Username del creatore: ${audio.creator_username}"
        findViewById<TextView>(R.id.textBpm).text = "BPM: ${audio.tags.bpm}"
        findViewById<TextView>(R.id.textDanceability).text =
            "Danzabilità: ${audio.tags.danceability}"
        findViewById<TextView>(R.id.textLoudness).text = "Rumorosità: ${audio.tags.loudness}"

        findViewById<TextView>(R.id.textLuogo).text =
            "Località: " + getLocationName(audio.longitude, audio.latitude)

        val maxMood = audio.tags.getMaxMood()
        findViewById<TextView>(R.id.textTopMood).text = "Mood: ${maxMood.first}"

        val maxGenre = audio.tags.getMaxGenre()
        findViewById<TextView>(R.id.textTopGenre).text = "Genere: ${maxGenre.first}"

        val maxInstrument = audio.tags.getMaxInstrument()
        findViewById<TextView>(R.id.textTopInstrument).text =
            "Strumento principale: ${maxInstrument.first}"

        observeAll()

        findViewById<Button>(R.id.btnPlay).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnStop).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnDelete).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnHide).visibility = View.VISIBLE

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val username = DataSingleton.username
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setDataSource(externalCacheDir?.absolutePath + "/" + username + "_" + audio.longitude + "_" + audio.latitude + ".mp3")
                prepare()
                start()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
        }

        findViewById<Button>(R.id.btnHide).setOnClickListener {
            val token = DataSingleton.token
            if (token != null) {
                audioViewModel.hideAudio(token, audio.id)
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val token = DataSingleton.token
            if (token != null) {
                showDeleteConfirmationDialog(token)
            }
        }
    }

    private fun observeAll() {
        audioViewModel.observeAudioHideLiveData().observe(this) {
            Toast.makeText(this, it.detail, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        }

        audioViewModel.observeAudioHideErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        audioViewModel.observeAudioDeleteLiveData().observe(this) {
            Toast.makeText(this, it.detail, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AppActivity::class.java)
            startActivity(intent)
            finish()
        }

        audioViewModel.observeAudioDeleteErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(token: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma")
            .setMessage("Sei sicuro di voler cancellare l'audio?")
            .setPositiveButton("Si") { dialog, which ->
                audioViewModel.deleteAudio(token, audio.id)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getLocationName(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var locationName = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                locationName = addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locationName
    }

    override fun onBackPressed() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
        super.onBackPressed()
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
