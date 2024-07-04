package com.derosa.progettolam.notification

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.derosa.progettolam.db.AudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.db.UploadDataEntity
import com.derosa.progettolam.pojo.UploadData
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.derosa.progettolam.util.DataSingleton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.Locale

class ConnectivityService : Service() {

    private lateinit var connectivityReceiver: BroadcastReceiver
    private lateinit var audioDatabase: AudioDatabase
    private lateinit var listUpload: List<UploadDataEntity>
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    private var lastHandledTime = 0L
    private val DEBOUNCE_INTERVAL = 5000L

    override fun onCreate() {
        super.onCreate()

        audioDatabase = AudioDatabase.getInstance(this)

        connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastHandledTime < DEBOUNCE_INTERVAL) {
                        return
                    }
                    lastHandledTime = currentTime

                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

                    if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected) {
                        if (DataSingleton.username != null) {
                            serviceScope.launch {
                                withContext(Dispatchers.IO) {
                                    listUpload = audioDatabase.uploadDataDao()
                                        .getAllUploadByUsernameInt(DataSingleton.username!!)

                                    if (listUpload.isNotEmpty()) {
                                        for (upload in listUpload) {
                                            uploadAudio(DataSingleton.username!!, upload)
                                        }

                                        val workRequest =
                                            OneTimeWorkRequest.Builder(WifiStateWorker::class.java)
                                                .build()
                                        WorkManager.getInstance(context).enqueue(workRequest)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)
    }

    private fun uploadAudio(username: String, upload: UploadDataEntity) {
        val token = DataSingleton.token
        if (token != null) {
            val mp3File =
                File(externalCacheDir?.absolutePath + "/" + username + "_" + upload.longitude + "_" + upload.latitude + ".mp3")

            if (mp3File.exists() && mp3File.canRead()) {
                val requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), mp3File)
                val fileUpload =
                    MultipartBody.Part.createFormData("file", mp3File.name, requestBody)

                uploadAudioAPI(token, upload.longitude!!, upload.latitude!!, fileUpload)
                deleteUpload(username, upload.longitude, upload.latitude)
            } else {
                Log.d("File Error", "MP3 file does not exist or cannot be read.")
            }
        }
    }

    private fun deleteUpload(username: String, longitude: Double, latitude: Double) {
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                audioDatabase.uploadDataDao()
                    .deleteUpload(username, longitude, latitude)
            }
        }
    }

    private fun uploadAudioAPI(
        token: String,
        longitude: Double,
        latitude: Double,
        audio: MultipartBody.Part
    ) {
        RetrofitInstance.api.uploadAudio(token, longitude, latitude, audio)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        serviceScope.launch {
                            withContext(Dispatchers.IO) {
                                val uploadData =
                                    gson.fromJson(
                                        response.body()!!.string(),
                                        UploadData::class.java
                                    )

                                audioDatabase.audioDataDao().insertAudio(
                                    AudioDataEntity(
                                        username = DataSingleton.username,
                                        longitude = longitude,
                                        latitude = latitude,
                                        locationName = getLocationName(longitude, latitude),
                                        bpm = uploadData.bpm,
                                        danceability = uploadData.danceability,
                                        loudness = uploadData.loudness,
                                        genre = uploadData.genre.getMaxGenre().first,
                                        mood = uploadData.mood.getMaxMood().first,
                                        instrument = uploadData.instrument.getMaxInstrument().first
                                    )
                                )

                                Log.e(
                                    "upload success",
                                    "upload effettuato " + uploadData.toString()
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("Fail Upload Audio", t.message.toString())
                }
            })
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

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(connectivityReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
