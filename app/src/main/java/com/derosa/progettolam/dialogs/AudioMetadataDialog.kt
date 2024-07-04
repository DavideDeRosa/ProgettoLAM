package com.derosa.progettolam.dialogs

import android.app.Dialog
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.derosa.progettolam.R
import com.derosa.progettolam.db.AllAudioDataEntity
import com.derosa.progettolam.pojo.AudioMetaData
import java.io.IOException
import java.util.Locale

class AudioMetadataDialog(
    private val audio: AudioMetaData?,
    private val audioDb: AllAudioDataEntity?
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setStyle(STYLE_NORMAL, R.style.DialogSlideAnim)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_audio_meta_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(true)

        if (audio != null) {
            initializeDialog(view)
        } else {
            initializeDialogDb(view)
        }

    }

    private fun initializeDialog(view: View) {
        view.findViewById<TextView>(R.id.textLongitude).text = "Longitudine: ${audio!!.longitude}"
        view.findViewById<TextView>(R.id.textLatitude).text = "Latitudine: ${audio.latitude}"
        view.findViewById<TextView>(R.id.textCreatorUsername).text =
            "Username del creatore: ${audio.creator_username}"
        view.findViewById<TextView>(R.id.textBpm).text = "BPM: ${audio.tags.bpm}"
        view.findViewById<TextView>(R.id.textDanceability).text =
            "Danzabilità: ${audio.tags.danceability}"
        view.findViewById<TextView>(R.id.textLoudness).text = "Rumorosità: ${audio.tags.loudness}"

        view.findViewById<TextView>(R.id.textLuogo).text =
            "Località: " + getLocationName(audio.longitude, audio.latitude)

        val maxMood = audio.tags.getMaxMood()
        view.findViewById<TextView>(R.id.textTopMood).text = "Mood: ${maxMood.first}"

        val maxGenre = audio.tags.getMaxGenre()
        view.findViewById<TextView>(R.id.textTopGenre).text = "Genere: ${maxGenre.first}"

        val maxInstrument = audio.tags.getMaxInstrument()
        view.findViewById<TextView>(R.id.textTopInstrument).text =
            "Strumento principale: ${maxInstrument.first}"
    }

    private fun initializeDialogDb(view: View) {
        view.findViewById<TextView>(R.id.textLongitude).text = "Longitudine: ${audioDb!!.longitude}"
        view.findViewById<TextView>(R.id.textLatitude).text = "Latitudine: ${audioDb.latitude}"
        view.findViewById<TextView>(R.id.textCreatorUsername).text =
            "Username del creatore: ${audioDb.username}"
        view.findViewById<TextView>(R.id.textBpm).text = "BPM: ${audioDb.bpm}"
        view.findViewById<TextView>(R.id.textDanceability).text =
            "Danzabilità: ${audioDb.danceability}"
        view.findViewById<TextView>(R.id.textLoudness).text = "Rumorosità: ${audioDb.loudness}"

        view.findViewById<TextView>(R.id.textLuogo).text =
            "Località: " + getLocationName(audioDb.longitude!!, audioDb.latitude!!)

        view.findViewById<TextView>(R.id.textTopMood).text = "Mood: ${audioDb.mood}"

        view.findViewById<TextView>(R.id.textTopGenre).text = "Genere: ${audioDb.genre}"

        view.findViewById<TextView>(R.id.textTopInstrument).text =
            "Strumento principale: ${audioDb.instrument}"
    }

    private fun getLocationName(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
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
}
