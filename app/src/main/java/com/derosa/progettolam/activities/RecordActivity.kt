package com.derosa.progettolam.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.derosa.progettolam.R
import com.derosa.progettolam.util.DataSingleton
import com.derosa.progettolam.viewmodel.AudioViewModel
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


class RecordActivity : AppCompatActivity() {

    private lateinit var btnLocal: Button
    private lateinit var txtLocal: TextView
    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnDeleteRecording: Button
    private lateinit var btnConfirmRecording: Button
    private lateinit var tvRecordingTime: TextView
    private lateinit var tvAudioLength: TextView
    private lateinit var tvCurrentPosition: TextView
    private lateinit var seekBar: SeekBar

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private var mediaRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]

        btnLocal = findViewById(R.id.btnLocal)
        txtLocal = findViewById(R.id.txtLocal)

        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnDeleteRecording = findViewById(R.id.btnDeleteRecording)
        btnConfirmRecording = findViewById(R.id.btnConfirmRecording)
        tvRecordingTime = findViewById(R.id.tvRecordingTime)
        tvAudioLength = findViewById(R.id.tvAudioLength)
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition)
        seekBar = findViewById(R.id.seekBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            startRecording()
        }

        btnStopRecording.setOnClickListener {
            stopRecording()
        }

        requestPermissions()
    }

    private fun startRecording() {
        val username = DataSingleton.username
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp4")
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopRecording() {
        val username = DataSingleton.username
        val token = DataSingleton.token
        if (token != null) {
            val recordedFilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp4"
            val mp3FilePath =
                externalCacheDir?.absolutePath + "/" + username + "_" + longitude + "_" + latitude + ".mp3"

            mediaRecorder?.apply {
                stop()
                release()
                mediaRecorder = null
            }

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
                val mp3File = File(mp3FilePath)

                if (mp3File.exists() && mp3File.canRead()) {
                    val requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), mp3File)
                    val fileUpload =
                        MultipartBody.Part.createFormData("file", mp3File.name, requestBody)

                    audioViewModel.uploadAudio(token, longitude, latitude, fileUpload)

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
                    Log.d("File Error", "MP3 file does not exist or cannot be read.")
                }
            } else {
                Log.d("FFmpeg", "Conversion to MP3 failed. Return code: $rc")
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
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

                txtLocal.text = "Longitudine: " + longitude + " Latitudine: " + latitude
                showUI()
            } else {
                getCurrentLocation()
            }
        }.addOnFailureListener {
            getCurrentLocation()
        }
    }

    private fun isLocationFresh(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < 30000
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

                txtLocal.text = "Longitudine: " + longitude + " Latitudine: " + latitude
                showUI()
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

                        txtLocal.text = "Longitudine: " + longitude + " Latitudine: " + latitude
                        showUI()

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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun showUI() { //modificare e gestire bene tutto il ciclo di registrazione dell'audio!
        btnStartRecording.visibility = View.VISIBLE
        btnStopRecording.visibility = View.VISIBLE
        btnPlayPause.visibility = View.VISIBLE
        btnDeleteRecording.visibility = View.VISIBLE
        btnConfirmRecording.visibility = View.VISIBLE
        tvRecordingTime.visibility = View.VISIBLE
        tvAudioLength.visibility = View.VISIBLE
        tvCurrentPosition.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)
        finish()
    }
}
