package com.derosa.progettolam.pojo

import kotlin.reflect.KProperty

data class Tags(
    val bpm: Int,
    val danceability: Double,
    val loudness: Double,
    val mood: Mood,
    val genre: Genre,
    val instrument: Instrument
) {
    fun getMaxMood(): Pair<String, Double> {
        return mood.getMax()
    }

    fun getMaxGenre(): Pair<String, Double> {
        return genre.getMax()
    }

    fun getMaxInstrument(): Pair<String, Double> {
        return instrument.getMax()
    }

    private fun Mood.getMax(): Pair<String, Double> {
        var maxKey = ""
        var maxValue = Double.MIN_VALUE
        this::class.members.forEach { member ->
            if (member is KProperty<*>) {
                val value = member.call(this) as? Double ?: return@forEach
                if (value > maxValue) {
                    maxValue = value
                    maxKey = member.name
                }
            }
        }
        return Pair(maxKey, maxValue)
    }

    private fun Genre.getMax(): Pair<String, Double> {
        var maxKey = ""
        var maxValue = Double.MIN_VALUE
        this::class.members.forEach { member ->
            if (member is KProperty<*>) {
                val value = member.call(this) as? Double ?: return@forEach
                if (value > maxValue) {
                    maxValue = value
                    maxKey = member.name
                }
            }
        }
        return Pair(maxKey, maxValue)
    }

    private fun Instrument.getMax(): Pair<String, Double> {
        var maxKey = ""
        var maxValue = Double.MIN_VALUE
        this::class.members.forEach { member ->
            if (member is KProperty<*>) {
                val value = member.call(this) as? Double ?: return@forEach
                if (value > maxValue) {
                    maxValue = value
                    maxKey = member.name
                }
            }
        }
        return Pair(maxKey, maxValue)
    }
}
