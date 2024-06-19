package com.derosa.progettolam.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.derosa.progettolam.R
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecordActivity : AppCompatActivity() {

    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlayPause: Button
    private lateinit var seekBar: SeekBar
    private lateinit var tvAudioLength: TextView
    private lateinit var tvCurrentPosition: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioFile: File

    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvAudioLength = findViewById(R.id.tvAudioLength)
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition)

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        btnStartRecording.setOnClickListener {
            startRecording()
            btnStartRecording.isEnabled = false
            btnStopRecording.isEnabled = true
            btnPlayPause.isEnabled = false
        }

        btnStopRecording.setOnClickListener {
            stopRecording()
            btnStartRecording.isEnabled = true
            btnStopRecording.isEnabled = false
            btnPlayPause.isEnabled = true
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startRecording() {
        val audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (audioDir != null) {
            audioFile = File.createTempFile("audio_", ".mp3", audioDir)
        }

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(this, "Audio recorded at: ${audioFile.absolutePath}", Toast.LENGTH_LONG).show()

        // Get audio duration
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.absolutePath)
            prepare()
        }
        val duration = mediaPlayer.duration
        mediaPlayer.release()

        tvAudioLength.text = formatTime(duration)
        seekBar.max = duration
    }

    private fun startPlayback() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        isPlaying = true
        btnPlayPause.text = "Pause"

        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            btnPlayPause.text = "Play"
            tvCurrentPosition.text = "00:00"
            seekBar.progress = 0
        }

        updateSeekBar()
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        isPlaying = false
        btnPlayPause.text = "Play"
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            seekBar.progress = it.currentPosition
            tvCurrentPosition.text = formatTime(it.currentPosition)
            if (isPlaying) {
                handler.postDelayed({ updateSeekBar() }, 50)
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onStop() {
        super.onStop()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
