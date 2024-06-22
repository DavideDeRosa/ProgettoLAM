
import android.app.Dialog
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.derosa.progettolam.R
import com.derosa.progettolam.pojo.AudioMetaData
import java.io.IOException
import java.util.Locale

class AudioMetaDataDialog(
    context: Context,
    private val audio: AudioMetaData
) : Dialog(context, R.style.DialogSlideAnim) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_audio_meta_data)

        // Adjust dialog window size and position
        val window = window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(true)

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
        val geocoder = Geocoder(context, Locale.getDefault())
        var locationName = ""

        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                locationName = addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locationName
    }
}
