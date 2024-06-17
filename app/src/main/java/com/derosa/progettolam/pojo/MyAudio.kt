package com.derosa.progettolam.pojo

import com.google.gson.annotations.SerializedName

data class MyAudio (

    @SerializedName("id")
    var id: Int,

    @SerializedName("longitude")
    var longitude: Double,

    @SerializedName("latitude")
    var latitude: Double,

    @SerializedName("hidden")
    var hidden: Boolean
)