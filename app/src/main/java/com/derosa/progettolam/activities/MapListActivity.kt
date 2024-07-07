package com.derosa.progettolam.activities

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.derosa.progettolam.R
import com.derosa.progettolam.adapters.MapListAdapter
import com.derosa.progettolam.adapters.SpacingItemDecoration
import com.derosa.progettolam.db.AllAudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.dialogs.AudioMetadataDialog
import com.derosa.progettolam.pojo.AudioMetaData
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import java.io.IOException
import java.util.Locale

class MapListActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var mapListAdapter: MapListAdapter
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_list)

        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMapList)
        mapListAdapter = MapListAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mapListAdapter
        recyclerView.addItemDecoration(SpacingItemDecoration(20))

        val token = DataSingleton.token
        if (token != null) {
            audioViewModel.allAudio(token)
        }

        observeAllAudio()
        observeAllAudioDb()
        observeAudioById()

        mapListAdapter.onItemClick = {
            id = it.id

            audioViewModel.getAllAudioByCoord(
                it.longitude,
                it.latitude
            )
        }
    }

    private fun observeAllAudio() {
        audioViewModel.observeAllAudioLiveData().observe(this) {
            mapListAdapter.setMapList(ArrayList(it))
        }

        audioViewModel.observeAllAudioErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun observeAllAudioDb() {
        audioViewModel.observeAllAudioDbLiveData().observe(this) { list ->
            if (list.isEmpty()) {
                val token = DataSingleton.token
                if (token != null) {
                    audioViewModel.getAudioById(token, id)
                }
            } else {
                val customDialog = AudioMetadataDialog(null, list[0])
                customDialog.show(supportFragmentManager, "AudioMetaDataDialog")
            }
        }
    }

    private fun observeAudioById() {
        audioViewModel.observeAudioByIdLiveData().observe(this) {
            saveIntoDb(it)
            val customDialog = AudioMetadataDialog(it, null)
            customDialog.show(supportFragmentManager, "AudioMetaDataDialog")
        }

        audioViewModel.observeAudioByIdErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun saveIntoDb(audio: AudioMetaData) {
        audioViewModel.insertAllAudioDb(
            AllAudioDataEntity(
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
    }
}