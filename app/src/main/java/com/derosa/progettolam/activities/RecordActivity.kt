package com.derosa.progettolam.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.derosa.progettolam.R
import java.io.File
import java.io.IOException
import java.util.*

class RecordActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var isPlaying = false

    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnDeleteRecording: Button
    private lateinit var btnConfirmRecording: Button
    private lateinit var tvRecordingTime: TextView
    private lateinit var tvAudioLength: TextView
    private lateinit var tvCurrentPosition: TextView
    private lateinit var seekBar: SeekBar

    private val handler = Handler()
    private var recordingStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnDeleteRecording = findViewById(R.id.btnDeleteRecording)
        btnConfirmRecording = findViewById(R.id.btnConfirmRecording)
        tvRecordingTime = findViewById(R.id.tvRecordingTime)
        tvAudioLength = findViewById(R.id.tvAudioLength)
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition)
        seekBar = findViewById(R.id.seekBar)

        btnStartRecording.setOnClickListener { startRecording() }
        btnStopRecording.setOnClickListener { stopRecording() }
        btnPlayPause.setOnClickListener { playPauseAudio() }
        btnDeleteRecording.setOnClickListener { deleteRecording() }
        btnConfirmRecording.setOnClickListener { confirmRecording() }

        requestPermissions()
        showInitialUI()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                audioFile = File(externalCacheDir?.absolutePath + "/audio.3gp")
                setOutputFile(audioFile?.absolutePath)
                try {
                    prepare()
                    start()
                    isRecording = true
                    recordingStartTime = System.currentTimeMillis()
                    updateRecordingTime()
                    showRecordingUI()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
                mediaRecorder = null
            }
            isRecording = false
            showPlaybackUI()
        }
    }

    private fun playPauseAudio() {
        if (audioFile != null && audioFile!!.exists()) {
            if (isPlaying) {
                mediaPlayer?.pause()
                btnPlayPause.text = "Play"
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioFile!!.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            btnPlayPause.text = "Play"
                            this@RecordActivity.isPlaying = false
                        }
                    }
                }
                mediaPlayer?.start()
                btnPlayPause.text = "Pause"
                updateSeekBar()
            }
            isPlaying = !isPlaying
        } else {
            Toast.makeText(this, "No audio recorded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteRecording() {
        mediaPlayer?.release()
        mediaPlayer = null
        audioFile?.delete()
        audioFile = null
        showInitialUI()
    }

    private fun confirmRecording() {
        // Upload the audio file or save it as required
        Toast.makeText(this, "Recording confirmed", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecordingTime() {
        handler.postDelayed({
            if (isRecording) {
                val elapsedTime = (System.currentTimeMillis() - recordingStartTime) / 1000
                val minutes = elapsedTime / 60
                val seconds = elapsedTime % 60
                tvRecordingTime.text =
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                updateRecordingTime()
            }
        }, 1000)
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            seekBar.max = it.duration
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (isPlaying) {
                        seekBar.progress = it.currentPosition
                        val currentPosition = it.currentPosition / 1000
                        val minutes = currentPosition / 60
                        val seconds = currentPosition % 60
                        tvCurrentPosition.text =
                            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        handler.postDelayed(this, 100)
                    }
                }
            }, 100)
        }
    }

    private fun showRecordingUI() {
        btnStartRecording.visibility = View.GONE
        tvRecordingTime.visibility = View.VISIBLE
        btnStopRecording.visibility = View.VISIBLE
    }

    private fun showPlaybackUI() {
        btnStopRecording.visibility = View.GONE
        tvRecordingTime.visibility = View.GONE
        tvAudioLength.visibility = View.VISIBLE
        tvCurrentPosition.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
        btnPlayPause.visibility = View.VISIBLE
        btnDeleteRecording.visibility = View.VISIBLE
        btnConfirmRecording.visibility = View.VISIBLE

        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile!!.absolutePath)
            prepare()
            val duration = duration / 1000
            val minutes = duration / 60
            val seconds = duration % 60
            tvAudioLength.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun showInitialUI() {
        btnStartRecording.visibility = View.VISIBLE
        tvRecordingTime.visibility = View.GONE
        btnStopRecording.visibility = View.GONE
        tvAudioLength.visibility = View.GONE
        tvCurrentPosition.visibility = View.GONE
        seekBar.visibility = View.GONE
        btnPlayPause.visibility = View.GONE
        btnDeleteRecording.visibility = View.GONE
        btnConfirmRecording.visibility = View.GONE

        tvAudioLength.text = String.format(Locale.getDefault(), "%02d:%02d", 0, 0)
        tvCurrentPosition.text = String.format(Locale.getDefault(), "%02d:%02d", 0, 0)
        seekBar.progress = 0
    }
}
