package com.derosa.progettolam.pojo

import kotlin.reflect.KProperty

data class Mood(
    val action: Double,
    val adventure: Double,
    val advertising: Double,
    val background: Double,
    val ballad: Double,
    val calm: Double,
    val children: Double,
    val christmas: Double,
    val commercial: Double,
    val cool: Double,
    val corporate: Double,
    val dark: Double,
    val deep: Double,
    val documentary: Double,
    val drama: Double,
    val dramatic: Double,
    val dream: Double,
    val emotional: Double,
    val energetic: Double,
    val epic: Double,
    val fast: Double,
    val film: Double,
    val `fun`: Double,
    val funny: Double,
    val game: Double,
    val groovy: Double,
    val happy: Double,
    val heavy: Double,
    val holiday: Double,
    val hopeful: Double,
    val inspiring: Double,
    val love: Double,
    val meditative: Double,
    val melancholic: Double,
    val melodic: Double,
    val motivational: Double,
    val movie: Double,
    val nature: Double,
    val party: Double,
    val positive: Double,
    val powerful: Double,
    val relaxing: Double,
    val retro: Double,
    val romantic: Double,
    val sad: Double,
    val sexy: Double,
    val slow: Double,
    val soft: Double,
    val soundscape: Double,
    val space: Double,
    val sport: Double,
    val summer: Double,
    val trailer: Double,
    val travel: Double,
    val upbeat: Double,
    val uplifting: Double
) {
    fun getMaxMood(): Pair<String, Double> {
        return getMax()
    }

    private fun getMax(): Pair<String, Double> {
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