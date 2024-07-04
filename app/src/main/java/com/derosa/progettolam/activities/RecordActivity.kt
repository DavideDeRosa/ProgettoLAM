package com.derosa.progettolam.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.derosa.progettolam.R
import com.derosa.progettolam.db.AudioDataEntity
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.db.UploadDataEntity
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.util.ExtraUtil
import com.derosa.progettolam.viewmodel.AudioViewModel
import com.derosa.progettolam.viewmodel.AudioViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.util.Locale


class RecordActivity : AppCompatActivity() {

    private lateinit var btnLocal: Button
    private lateinit var txtLocal: TextView
    private lateinit var btnStartRecording: Button
    private lateinit var txtDuration: TextView
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlay: Button
    private lateinit var btnStop: Button
    private lateinit var btnDeleteRecording: Button
    private lateinit var btnConfirmRecording: Button

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordedFilePath = ""
    private var mp3FilePath = ""

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var startTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val audioDatabase = AudioDatabase.getInstance(this)
        val viewModelFactory = AudioViewModelFactory(audioDatabase)
        audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        handler = Handler()

        btnLocal = findViewById(R.id.btnLocal)
        txtLocal = findViewById(R.id.txtLocal)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        txtDuration = findViewById(R.id.txtDuration)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnPlay = findViewById(R.id.btnPlayAudio)
        btnStop = findViewById(R.id.btnStopAudio)
        btnDeleteRecording = findViewById(R.id.btnDeleteRecording)
        btnConfirmRecording = findViewById(R.id.btnConfirmRecording)

        observeUpload()

        btnLocal.setOnClickListener {
            if (checkPermission()) {
                if (isLocationEnabled()) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Abilita i servizi di localizzazione", Toast.LENGTH_SHORT)
                        .show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                askPermission()
            }
        }

        btnStartRecording.setOnClickListener {
            if (checkPermissionAudio()) {
                startRecording()
                showUIAfterStartRecording()
            } else {
                askPermissionAudio()
            }
        }

        btnStopRecording.setOnClickListener {
            stopRecording()
            showUIAfterStopRecording()
        }

        btnPlay.setOnClickListener {
            playRecording()
        }

        btnStop.setOnClickListener {
            stopAudioRecording()
        }

        btnDeleteRecording.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        btnConfirmRecording.setOnClickListener {
            confirmRecording()
        }
    }

    private fun observeUpload() {
        audioViewModel.observeFileCorrectlyUploadedLiveData().observe(this) {
            Toast.makeText(this, "Caricamento avvenuto con successo!", Toast.LENGTH_SHORT).show()

            audioViewModel.insertAudioDb(
                AudioDataEntity(
                    username = DataSingleton.username,
                    longitude = longitude,
                    latitude = latitude,
                    locationName = getLocationName(longitude, latitude),
                    bpm = it.bpm,
                    danceability = it.danceability,
                    loudness = it.loudness,
                    genre = it.genre.getMaxGenre().first,
                    mood = it.mood.getMaxMood().first,
                    instrument = it.instrument.getMaxInstrument().first
                )
            )

            goToAppActivity()
        }

        audioViewModel.observeFileCorrectlyUploadedErrorLiveData().observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            deleteRecording()
            goToLogin()
        }
    }

    private fun startRecording() {
        val username = DataSingleton.username
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                recordedFilePath =
                    externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp4"

                setOutputFile(recordedFilePath)
                try {
                    prepare()
                    start()

                    startTime = System.currentTimeMillis()
                    startTimer()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startTimer() {
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - startTime
                txtDuration.text = "Durata: " + formatDuration(elapsedMillis)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        handler.removeCallbacks(runnable)
        val elapsedMillis = System.currentTimeMillis() - startTime
        txtDuration.text = "Durata: " + formatDuration(elapsedMillis)

        val username = DataSingleton.username
        mp3FilePath =
            externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp3"

        val ffmpegCommand = arrayOf(
            "-i",
            recordedFilePath,
            "-codec:a",
            "libmp3lame",
            "-qscale:a",
            "2",
            mp3FilePath
        )
        val rc = FFmpeg.execute(ffmpegCommand)

        if (rc == Config.RETURN_CODE_SUCCESS) {
            Log.d("FFmpeg", "Conversion to MP3 succeeded.")

            val mp4File = File(recordedFilePath)
            if (mp4File.exists()) {
                val deleted = mp4File.delete()
                if (deleted) {
                    Log.d("File Deletion", "MP4 file deleted successfully.")
                } else {
                    Log.d("File Deletion", "Failed to delete MP4 file.")
                }
            }
        } else {
            Log.d("FFmpeg", "Conversion to MP3 failed. Return code: $rc")

            Toast.makeText(this, "C'è stato un errore!", Toast.LENGTH_SHORT).show()
            goToAppActivity()
        }
    }

    private fun formatDuration(durationInMillis: Long): String {
        val seconds = (durationInMillis / 1000) % 60
        val minutes = (durationInMillis / (1000 * 60)) % 60
        val hours = (durationInMillis / (1000 * 60 * 60)) % 24

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun playRecording() {
        val audioFile = File(mp3FilePath)

        if (audioFile.exists()) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setDataSource(mp3FilePath)
                prepare()
                start()

                btnPlay.isEnabled = false
                btnPlay.setBackgroundResource(R.color.green_opaque)

                setOnCompletionListener {
                    btnPlay.isEnabled = true
                    btnPlay.setBackgroundResource(R.color.green)
                }
            }
        } else {
            Toast.makeText(this, "Il file audio non esiste", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAudioRecording() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null

            btnPlay.isEnabled = true
            btnPlay.setBackgroundResource(R.color.green)
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma")
            .setMessage("Sei sicuro di voler cancellare l'audio?")
            .setPositiveButton("Si") { dialog, which ->
                deleteRecording()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteRecording() {
        val audioFile =
            File(mp3FilePath)

        if (audioFile.exists()) {
            val deleted = audioFile.delete()
            if (deleted) {
                Log.d("File Deletion", "Audio file deleted successfully.")
            } else {
                Log.d("File Deletion Error", "Failed to delete Audio file.")
            }
        }

        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun confirmRecording() {
        if (ExtraUtil.isWifiConnected(this)) {
            uploadAudio()
        } else {
            showUploadConfirmationDialog()
        }
    }

    private fun showUploadConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Conferma")
            .setMessage("Non sei connesso ad una rete Wi-Fi. Vuoi continuare il caricamento con i dati mobili?")
            .setPositiveButton("Si") { dialog, which ->
                uploadAudio()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                saveUploadDb()
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadAudio() {
        val token = DataSingleton.token
        if (token != null) {
            val mp3File = File(mp3FilePath)

            if (mp3File.exists() && mp3File.canRead()) {
                val requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), mp3File)
                val fileUpload =
                    MultipartBody.Part.createFormData("file", mp3File.name, requestBody)

                audioViewModel.uploadAudio(token, longitude, latitude, fileUpload)
            } else {
                Log.d("File Error", "MP3 file does not exist or cannot be read.")

                Toast.makeText(this, "C'è stato un errore!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUploadDb() {
        audioViewModel.insertUploadDb(
            UploadDataEntity(
                username = DataSingleton.username,
                longitude = longitude,
                latitude = latitude
            )
        )

        Toast.makeText(this, "Audio salvato in memoria correttamente!", Toast.LENGTH_SHORT).show()
        goToAppActivity()
    }

    private fun checkPermissionAudio(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermissionAudio() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.RECORD_AUDIO
            ), 0
        )
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && isLocationFresh(location)) {
                longitude = location.longitude
                latitude = location.latitude

                setLocal()
            } else {
                getCurrentLocation()
            }
        }.addOnFailureListener {
            getCurrentLocation()
        }
    }

    private fun isLocationFresh(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < 10000
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
                    return this
                }

                override fun isCancellationRequested(): Boolean {
                    return false
                }
            }).addOnSuccessListener { location ->
            if (location != null) {
                longitude = location.longitude
                latitude = location.latitude

                setLocal()
            } else {
                startLocationUpdates()
            }
        }.addOnFailureListener {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        longitude = location.longitude
                        latitude = location.latitude

                        setLocal()

                        fusedLocationClient.removeLocationUpdates(this)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Non è possibile trovare la tua ultima posizione",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            null
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (isLocationEnabled()) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Abilita i servizi di localizzazione", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Permesso negato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLocal() {
        txtLocal.text = "Località: " + getLocationName(longitude, latitude)
        showUIAfterLocal()
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun showUIAfterLocal() {
        btnLocal.isEnabled = false
        btnLocal.setBackgroundResource(R.color.grey_opaque)

        btnStartRecording.visibility = View.VISIBLE
    }

    private fun showUIAfterStartRecording() {
        btnStartRecording.isEnabled = false
        btnStartRecording.setBackgroundResource(R.color.grey_opaque)

        btnStopRecording.visibility = View.VISIBLE
    }

    private fun showUIAfterStopRecording() {
        btnStopRecording.isEnabled = false
        btnStopRecording.setBackgroundResource(R.color.grey_opaque)

        btnPlay.visibility = View.VISIBLE
        btnStop.visibility = View.VISIBLE
        btnDeleteRecording.visibility = View.VISIBLE
        btnConfirmRecording.visibility = View.VISIBLE
    }

    private fun goToAppActivity() {
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
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
        goToAppActivity()
    }
}
