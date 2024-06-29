package com.derosa.progettolam.pojo

import kotlin.reflect.KProperty

data class Instrument(
    val accordion: Double,
    val acousticbassguitar: Double,
    val acousticguitar: Double,
    val bass: Double,
    val beat: Double,
    val bell: Double,
    val bongo: Double,
    val brass: Double,
    val cello: Double,
    val clarinet: Double,
    val classicalguitar: Double,
    val computer: Double,
    val doublebass: Double,
    val drummachine: Double,
    val drums: Double,
    val electricguitar: Double,
    val electricpiano: Double,
    val flute: Double,
    val guitar: Double,
    val harmonica: Double,
    val harp: Double,
    val horn: Double,
    val keyboard: Double,
    val oboe: Double,
    val orchestra: Double,
    val organ: Double,
    val pad: Double,
    val percussion: Double,
    val piano: Double,
    val pipeorgan: Double,
    val rhodes: Double,
    val sampler: Double,
    val saxophone: Double,
    val strings: Double,
    val synthesizer: Double,
    val trombone: Double,
    val trumpet: Double,
    val viola: Double,
    val violin: Double,
    val voice: Double
) {
    fun getMaxInstrument(): Pair<String, Double> {
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