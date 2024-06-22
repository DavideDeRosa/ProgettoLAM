package com.derosa.progettolam.activities

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.derosa.progettolam.R
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.AudioViewModel
import java.io.IOException
import java.util.Locale

class AudioMetaDataActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_meta_data)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]

        id = intent.getIntExtra("id", 0)

        val token = DataSingleton.token
        if (token != null) {
            audioViewModel.getAudioById(token, id)
        }

        audioViewModel.observeAudioByIdLiveData().observe(this) {
            displayAudioData(it)
        }

        audioViewModel.observeAudioByIdErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun displayAudioData(audio: AudioMetaData) {
        findViewById<TextView>(R.id.textLongitude).text = "Longitudine: ${audio.longitude}"
        findViewById<TextView>(R.id.textLatitude).text = "Latitudine: ${audio.latitude}"
        findViewById<TextView>(R.id.textCreatorUsername).text = "Username del creatore: ${audio.creator_username}"
        findViewById<TextView>(R.id.textBpm).text = "BPM: ${audio.tags.bpm}"
        findViewById<TextView>(R.id.textDanceability).text = "Danzabilità: ${audio.tags.danceability}"
        findViewById<TextView>(R.id.textLoudness).text = "Rumorosità: ${audio.tags.loudness}"

        findViewById<TextView>(R.id.textLuogo).text = "Località: " + getLocationName(audio.longitude, audio.latitude)

        val maxMood = audio.tags.getMaxMood()
        findViewById<TextView>(R.id.textTopMood).text = "Mood: ${maxMood.first}"

        val maxGenre = audio.tags.getMaxGenre()
        findViewById<TextView>(R.id.textTopGenre).text = "Genere: ${maxGenre.first}"

        val maxInstrument = audio.tags.getMaxInstrument()
        findViewById<TextView>(R.id.textTopInstrument).text = "Strumento principale: ${maxInstrument.first}"
    }

    private fun getLocationName(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var locationName = ""

        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    locationName = addresses[0].getAddressLine(0)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locationName
    }
}