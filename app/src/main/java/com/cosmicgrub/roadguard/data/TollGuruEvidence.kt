package com.cosmicgrub.roadguard.data

import org.json.JSONObject

data class TollGuruConfig(
    val apiKey: String,
    val endpoint: String = "https://apis.tollguru.com/toll/v2/complete-polyline-from-mapping-service"
)

interface TollGuruHttpClient {
    suspend fun verifyPolyline(url: String, apiKey: String, polyline: String): String
}

class TollGuruEvidence(
    private val config: TollGuruConfig,
    private val http: TollGuruHttpClient
) : IndependentTollEvidence {
    override suspend fun verify(geometry: List<GeoPoint>): TollVerification {
        if (geometry.size < 2) return TollVerification.Unknown("Route geometry is incomplete")
        if (config.apiKey.isBlank()) return TollVerification.Unknown("TollGuru API key is not configured")

        val body = http.verifyPolyline(
            config.endpoint,
            config.apiKey,
            GooglePolyline.encode(geometry)
        )
        return parse(body)
    }

    internal fun parse(json: String): TollVerification {
        val root = JSONObject(json)
        if (root.optString("status").equals("error", ignoreCase = true)) {
            return TollVerification.Unknown("Toll evidence provider returned an error")
        }

        val route = root.optJSONObject("route")
            ?: root.optJSONObject("summary")?.optJSONObject("route")
            ?: return TollVerification.Unknown("Toll evidence response omitted route coverage")

        if (!route.has("hasTolls")) {
            return TollVerification.Unknown("Toll evidence response omitted toll status")
        }

        return if (route.getBoolean("hasTolls")) {
            TollVerification.TollDetected("Independent toll evidence detected tolls")
        } else {
            TollVerification.VerifiedZero
        }
    }
}

object GooglePolyline {
    fun encode(points: List<GeoPoint>): String {
        var lastLat = 0
        var lastLng = 0
        val out = StringBuilder()
        for (point in points) {
            val lat = kotlin.math.round(point.latitude * 1e5).toInt()
            val lng = kotlin.math.round(point.longitude * 1e5).toInt()
            encodeValue(lat - lastLat, out)
            encodeValue(lng - lastLng, out)
            lastLat = lat
            lastLng = lng
        }
        return out.toString()
    }

    private fun encodeValue(value: Int, out: StringBuilder) {
        var shifted = if (value < 0) (value shl 1).inv() else value shl 1
        while (shifted >= 0x20) {
            out.append(((0x20 or (shifted and 0x1f)) + 63).toChar())
            shifted = shifted shr 5
        }
        out.append((shifted + 63).toChar())
    }
}
