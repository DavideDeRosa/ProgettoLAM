package com.derosa.progettolam.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.derosa.progettolam.databinding.MyAudioItemBinding

class MyAudioAdapter : RecyclerView.Adapter<MyAudioAdapter.MyAudioViewHolder>() {

    inner class MyAudioViewHolder(val binding: MyAudioItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var myAudioList = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setMyAudioList(list: ArrayList<String>) {
        myAudioList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MyAudioItemBinding.inflate(inflater, parent, false)
        return MyAudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyAudioViewHolder, position: Int) {
        holder.binding.txtLuogoMyAudio.text = myAudioList[position]
    }

    override fun getItemCount(): Int {
        return myAudioList.size
    }
}
