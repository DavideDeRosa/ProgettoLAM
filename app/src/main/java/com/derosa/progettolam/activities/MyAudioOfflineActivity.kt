package com.derosa.progettolam.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.db.AudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import java.io.File

class MyAudioOfflineActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_audio_offline)

        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]

        val id = intent.getIntExtra("audio_id_offline", 0)

        audioViewModel.getAudioById(id)

        observeAudioByIdDb()
    }

    private fun observeAudioByIdDb() {
        audioViewModel.observeAudioDbLiveData().observe(this) {
            initializeFromDb(it)
        }
    }

    private fun initializeFromDb(audio: AudioDataEntity) {
        findViewById<TextView>(R.id.textInizialeOffline).visibility = View.VISIBLE

        findViewById<TextView>(R.id.textLongitudeOffline).text = "Longitudine: ${audio.longitude}"
        findViewById<TextView>(R.id.textLatitudeOffline).text = "Latitudine: ${audio.latitude}"
        findViewById<TextView>(R.id.textCreatorUsernameOffline).text =
            "Username del creatore: ${audio.username}"
        findViewById<TextView>(R.id.textBpmOffline).text = "BPM: ${audio.bpm}"
        findViewById<TextView>(R.id.textDanceabilityOffline).text =
            "Danzabilità: ${audio.danceability}"
        findViewById<TextView>(R.id.textLoudnessOffline).text = "Rumorosità: ${audio.loudness}"
        findViewById<TextView>(R.id.textLuogoOffline).text =
            "Località: ${audio.locationName}"
        findViewById<TextView>(R.id.textTopMoodOffline).text = "Mood: ${audio.mood}"
        findViewById<TextView>(R.id.textTopGenreOffline).text = "Genere: ${audio.genre}"
        findViewById<TextView>(R.id.textTopInstrumentOffline).text =
            "Strumento principale: ${audio.instrument}"

        findViewById<Button>(R.id.btnPlayOffline).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnStopOffline).visibility = View.VISIBLE

        findViewById<Button>(R.id.btnPlayOffline).setOnClickListener {
            val username = audio.username
            val audioFilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + audio.longitude + "_" + audio.latitude + ".mp3"
            val audioFile = File(audioFilePath)

            if (audioFile.exists()) {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.apply {
                    setDataSource(audioFilePath)
                    prepare()
                    start()

                    findViewById<Button>(R.id.btnPlayOffline).isEnabled = false
                    findViewById<Button>(R.id.btnPlayOffline).setBackgroundResource(R.color.green_opaque)

                    setOnCompletionListener {
                        findViewById<Button>(R.id.btnPlayOffline).isEnabled = true
                        findViewById<Button>(R.id.btnPlayOffline).setBackgroundResource(R.color.green)
                    }
                }
            } else {
                Toast.makeText(this, "Il file audio non esiste", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnStopOffline).setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null

                findViewById<Button>(R.id.btnPlayOffline).isEnabled = true
                findViewById<Button>(R.id.btnPlayOffline).setBackgroundResource(R.color.green)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }

        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
        mediaPlayer = null
    }
}