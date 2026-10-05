package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent
import com.cosmicgrub.roadguard.domain.RoadEventType
import org.json.JSONObject
import java.time.Instant

object NwsAlertParser {
    fun events(json: String, source: String, fetchedAtEpochMs: Long): List<RoadEvent> {
        val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
        return buildList {
            for (i in 0 until features.length()) {
                val feature = features.optJSONObject(i) ?: continue
                val properties = feature.optJSONObject("properties") ?: continue
                val status = properties.optString("status")
                if (status.equals("Test", true)) continue
                val eventName = properties.optString("event")
                val type = classify(eventName) ?: continue
                val point = representativePoint(feature.optJSONObject("geometry")) ?: continue
                val effective = instant(properties.optString("effective"))
                    ?: instant(properties.optString("onset"))
                    ?: fetchedAtEpochMs
                val expires = instant(properties.optString("expires"))
                    ?: instant(properties.optString("ends"))
                add(RoadEvent(
                    id = properties.optString("id", feature.optString("id", "nws-$i")),
                    type = type,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    severity = severity(properties.optString("severity")),
                    confidence = certainty(properties.optString("certainty")),
                    source = source,
                    firstSeenEpochMs = effective,
                    lastVerifiedEpochMs = fetchedAtEpochMs,
                    expiresAtEpochMs = expires
                ))
            }
        }
    }

    private fun classify(name: String): RoadEventType? = when {
        name.contains("flood", true) -> RoadEventType.FLOODING
        name.contains("ice", true) || name.contains("freezing", true) -> RoadEventType.ICE
        name.contains("snow", true) || name.contains("blizzard", true) || name.contains("winter", true) -> RoadEventType.SNOW
        name.contains("tornado", true) || name.contains("thunderstorm", true) ||
            name.contains("hurricane", true) || name.contains("tropical", true) ||
            name.contains("wind", true) -> RoadEventType.SEVERE_WEATHER
        else -> null
    }

    private fun severity(value: String): Int = when (value.lowercase()) {
        "extreme" -> 5
        "severe" -> 4
        "moderate" -> 3
        "minor" -> 2
        else -> 1
    }

    private fun certainty(value: String): Double = when (value.lowercase()) {
        "observed" -> 1.0
        "likely" -> 0.9
        "possible" -> 0.65
        "unlikely" -> 0.35
        else -> 0.5
    }

    private fun representativePoint(geometry: JSONObject?): GeoPoint? {
        geometry ?: return null
        val coords = geometry.optJSONArray("coordinates") ?: return null
        return when (geometry.optString("type")) {
            "Point" -> if (coords.length() >= 2) GeoPoint(coords.optDouble(1), coords.optDouble(0)) else null
            "Polygon" -> {
                val ring = coords.optJSONArray(0) ?: return null
                val points = (0 until ring.length()).mapNotNull { j ->
                    ring.optJSONArray(j)?.takeIf { it.length() >= 2 }?.let { GeoPoint(it.optDouble(1), it.optDouble(0)) }
                }
                if (points.isEmpty()) null else GeoPoint(points.map { it.latitude }.average(), points.map { it.longitude }.average())
            }
            "MultiPolygon" -> {
                val polygon = coords.optJSONArray(0) ?: return null
                representativePoint(JSONObject().put("type", "Polygon").put("coordinates", polygon))
            }
            else -> null
        }
    }

    private fun instant(value: String): Long? =
        value.takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
}
