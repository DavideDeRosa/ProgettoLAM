package com.derosa.progettolam.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.derosa.progettolam.R
import com.derosa.progettolam.databinding.MyAudioItemBinding
import com.derosa.progettolam.db.AudioDataEntity
import java.io.IOException
import java.util.Locale

class AudioOfflineAdapter(private val context: Context?) :
    RecyclerView.Adapter<AudioOfflineAdapter.MyAudioViewHolder>() {

    inner class MyAudioViewHolder(val binding: MyAudioItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var audioList = ArrayList<AudioDataEntity>()
    var onItemClick: ((AudioDataEntity) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setAudioList(list: ArrayList<AudioDataEntity>) {
        audioList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MyAudioItemBinding.inflate(inflater, parent, false)
        return MyAudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyAudioViewHolder, position: Int) {
        holder.binding.root.setBackgroundResource(R.drawable.rounded_black_border)

        var location = getLocationName(
            audioList[position].longitude!!,
            audioList[position].latitude!!
        )

        if (location == "") {
            holder.binding.txtLuogoMyAudio.text =
                "Luogo sconosciuto! Longitudine: " + audioList[position].longitude + " Latitudine: " + audioList[position].latitude
        } else {
            holder.binding.txtLuogoMyAudio.text = location
        }

        holder.itemView.setOnClickListener {
            onItemClick!!.invoke(audioList[position])
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    private fun getLocationName(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(context!!, Locale.getDefault())
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