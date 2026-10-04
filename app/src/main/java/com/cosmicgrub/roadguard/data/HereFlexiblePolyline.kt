package com.cosmicgrub.roadguard.data

import kotlin.math.pow

object HereFlexiblePolyline {
    private const val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    fun decode(encoded: String): List<GeoPoint> {
        require(encoded.isNotEmpty())
        var index = 0
        val version = decodeUnsigned(encoded) { index }.also { index = it.next }.value
        require(version == 1L) { "Unsupported flexible polyline version: $version" }
        val header = decodeUnsigned(encoded) { index }.also { index = it.next }.value
        val precision = (header and 15).toInt()
        val thirdDimension = ((header shr 4) and 7).toInt()
        val thirdPrecision = ((header shr 7) and 15).toInt()
        val factor = 10.0.pow(precision)
        val thirdFactor = 10.0.pow(thirdPrecision)
        var lat = 0L
        var lng = 0L
        var third = 0L
        val points = mutableListOf<GeoPoint>()
        while (index < encoded.length) {
            val a = decodeSigned(encoded, index); index = a.next; lat += a.value
            val b = decodeSigned(encoded, index); index = b.next; lng += b.value
            if (thirdDimension != 0) {
                val c = decodeSigned(encoded, index); index = c.next; third += c.value
                third.toDouble() / thirdFactor
            }
            points += GeoPoint(lat / factor, lng / factor)
        }
        return points
    }

    private data class Decoded(val value: Long, val next: Int)

    private fun decodeUnsigned(encoded: String, indexProvider: () -> Int): Decoded =
        decodeUnsignedAt(encoded, indexProvider())

    private fun decodeUnsignedAt(encoded: String, start: Int): Decoded {
        var result = 0L
        var shift = 0
        var index = start
        while (index < encoded.length) {
            val value = alphabet.indexOf(encoded[index++])
            require(value >= 0) { "Invalid flexible polyline character" }
            result = result or ((value and 0x1f).toLong() shl shift)
            if ((value and 0x20) == 0) return Decoded(result, index)
            shift += 5
        }
        error("Invalid flexible polyline encoding")
    }

    private fun decodeSigned(encoded: String, start: Int): Decoded {
        val decoded = decodeUnsignedAt(encoded, start)
        val value = if ((decoded.value and 1L) != 0L) -(decoded.value shr 1) - 1 else decoded.value shr 1
        return Decoded(value, decoded.next)
    }
}
