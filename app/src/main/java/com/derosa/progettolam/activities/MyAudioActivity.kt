package com.derosa.progettolam.activities

import android.app.AlertDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.db.AudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import java.io.File
import java.io.IOException
import java.util.Locale

class MyAudioActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var audio: AudioMetaData
    private lateinit var audioDb: AudioDataEntity
    private var mediaPlayer: MediaPlayer? = null
    private var id = 0
    private var longitude = 0.0
    private var latitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_audio)

        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]

        id = intent.getIntExtra("audio_id", 0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        latitude = intent.getDoubleExtra("latitude", 0.0)

        val username = DataSingleton.username
        if (username != null) {
            audioViewModel.getAudioByCoordDb(username, longitude, latitude)
        }

        observeAudioByCoordDb()
        observeAudioById()
    }

    private fun observeAudioByCoordDb() {
        audioViewModel.observeAudioByCoordDbLiveData().observe(this) {
            if (it.isEmpty()) {
                val token = DataSingleton.token
                if (token != null) {
                    audioViewModel.getAudioById(token, id)
                }
            } else {
                audioDb = it[0]
                initializeFromDb()
            }
        }
    }

    private fun observeAudioById() {
        audioViewModel.observeAudioByIdLiveData().observe(this) {
            audio = it
            saveIntoDb(it)
            initialize()
        }

        audioViewModel.observeAudioByIdErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun saveIntoDb(audio: AudioMetaData) {
        audioViewModel.insertAudioDb(
            AudioDataEntity(
                id = audio.id,
                username = audio.creator_username,
                longitude = audio.longitude,
                latitude = audio.latitude,
                locationName = getLocationName(audio.longitude, audio.latitude),
                bpm = audio.tags.bpm,
                danceability = audio.tags.danceability,
                loudness = audio.tags.loudness,
                genre = audio.tags.genre.getMaxGenre().first,
                instrument = audio.tags.instrument.getMaxInstrument().first,
                mood = audio.tags.mood.getMaxMood().first
            )
        )
    }

    private fun initializeFromDb() {
        findViewById<TextView>(R.id.textIniziale).visibility = View.VISIBLE

        findViewById<TextView>(R.id.textLongitude).text = "Longitudine: ${audioDb.longitude}"
        findViewById<TextView>(R.id.textLatitude).text = "Latitudine: ${audioDb.latitude}"
        findViewById<TextView>(R.id.textCreatorUsername).text =
            "Username del creatore: ${audioDb.username}"
        findViewById<TextView>(R.id.textBpm).text = "BPM: ${audioDb.bpm}"
        findViewById<TextView>(R.id.textDanceability).text = "Danzabilità: ${audioDb.danceability}"
        findViewById<TextView>(R.id.textLoudness).text = "Rumorosità: ${audioDb.loudness}"
        findViewById<TextView>(R.id.textLuogo).text =
            "Località: " + getLocationName(audioDb.longitude!!, audioDb.latitude!!)
        findViewById<TextView>(R.id.textTopMood).text = "Mood: ${audioDb.mood}"
        findViewById<TextView>(R.id.textTopGenre).text = "Genere: ${audioDb.genre}"
        findViewById<TextView>(R.id.textTopInstrument).text =
            "Strumento principale: ${audioDb.instrument}"

        observeAll()

        findViewById<Button>(R.id.btnPlay).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnStop).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnDelete).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnHide).visibility = View.VISIBLE

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val username = DataSingleton.username
            val audioFilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + audioDb.longitude + "_" + audioDb.latitude + ".mp3"
            val audioFile = File(audioFilePath)

            if (audioFile.exists()) {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.apply {
                    setDataSource(audioFilePath)
                    prepare()
                    start()

                    findViewById<Button>(R.id.btnPlay).isEnabled = false
                    findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green_opaque)

                    setOnCompletionListener {
                        findViewById<Button>(R.id.btnPlay).isEnabled = true
                        findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green)
                    }
                }
            } else {
                Toast.makeText(this, "Il file audio non esiste", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null

                findViewById<Button>(R.id.btnPlay).isEnabled = true
                findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green)
            }
        }

        findViewById<Button>(R.id.btnHide).setOnClickListener {
            val token = DataSingleton.token
            if (token != null) {
                audioViewModel.hideAudio(token, id)
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val token = DataSingleton.token
            if (token != null) {
                showDeleteConfirmationDialog(token)
            }
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
            val audioFilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + audio.longitude + "_" + audio.latitude + ".mp3"
            val audioFile = File(audioFilePath)

            if (audioFile.exists()) {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.apply {
                    setDataSource(audioFilePath)
                    prepare()
                    start()

                    findViewById<Button>(R.id.btnPlay).isEnabled = false
                    findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green_opaque)

                    setOnCompletionListener {
                        findViewById<Button>(R.id.btnPlay).isEnabled = true
                        findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green)
                    }
                }
            } else {
                Toast.makeText(this, "Il file audio non esiste", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null

                findViewById<Button>(R.id.btnPlay).isEnabled = true
                findViewById<Button>(R.id.btnPlay).setBackgroundResource(R.color.green)
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
            goToAppActivity()
        }

        audioViewModel.observeAudioHideErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }

        audioViewModel.observeAudioDeleteLiveData().observe(this) {
            Toast.makeText(this, it.detail, Toast.LENGTH_SHORT).show()
            deleteAudio()

            val username = DataSingleton.username
            if (username != null) {
                audioViewModel.deleteAudioDb(username, longitude, latitude)
            }

            goToAppActivity()
        }

        audioViewModel.observeAudioDeleteErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun showDeleteConfirmationDialog(token: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma")
            .setMessage("Sei sicuro di voler cancellare l'audio?")
            .setPositiveButton("Si") { dialog, which ->
                audioViewModel.deleteAudio(token, id)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAudio() {
        val username = DataSingleton.username
        val audioFile =
            File(externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp3")

        if (audioFile.exists()) {
            val deleted = audioFile.delete()
            if (deleted) {
                Log.d("File Deletion", "Audio file deleted successfully.")
            } else {
                Log.d("File Deletion Error", "Failed to delete Audio file.")
            }
        }
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

    private fun goToLogin() {
        DataSingleton.token = null
        DataSingleton.username = null

        ExtraUtil.clearTokenAndUsername(this)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToAppActivity() {
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
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

        goToAppActivity()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
        mediaPlayer = null
    }
}