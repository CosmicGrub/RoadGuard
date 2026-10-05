package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent
import com.cosmicgrub.roadguard.domain.RoadEventType
import org.json.JSONObject
import java.time.Instant

object WzdxParser {
    fun events(json: String, source: String, fetchedAtEpochMs: Long): List<RoadEvent> {
        val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
        return buildList {
            for (i in 0 until features.length()) {
                val feature = features.optJSONObject(i) ?: continue
                val properties = feature.optJSONObject("properties") ?: continue
                val core = properties.optJSONObject("core_details") ?: properties
                val status = core.optString("event_status", properties.optString("event_status"))
                if (status.equals("completed", true) || status.equals("cancelled", true)) continue
                val geometry = feature.optJSONObject("geometry") ?: continue
                val point = representativePoint(geometry) ?: continue
                val id = feature.optString("id", core.optString("id", "wzdx-$i"))
                val type = classify(properties, core)
                val start = instant(core.optString("start_date")) ?: fetchedAtEpochMs
                val end = instant(core.optString("end_date"))
                add(RoadEvent(
                    id = id,
                    type = type,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    severity = severity(type),
                    confidence = 0.9,
                    source = source,
                    firstSeenEpochMs = start,
                    lastVerifiedEpochMs = fetchedAtEpochMs,
                    expiresAtEpochMs = end
                ))
            }
        }
    }

    private fun classify(properties: JSONObject, core: JSONObject): RoadEventType {
        val eventType = properties.optString("road_event_type")
        val laneClosures = core.optJSONArray("vehicle_impact")?.length() ?: 0
        val description = core.optString("description")
        return when {
            eventType.contains("detour", true) -> RoadEventType.ROAD_CLOSURE
            description.contains("closed", true) -> RoadEventType.ROAD_CLOSURE
            laneClosures > 0 -> RoadEventType.LANE_CLOSURE
            else -> RoadEventType.CONSTRUCTION
        }
    }

    private fun severity(type: RoadEventType) = when (type) {
        RoadEventType.ROAD_CLOSURE -> 5
        RoadEventType.LANE_CLOSURE -> 4
        else -> 3
    }

    private fun representativePoint(geometry: JSONObject): GeoPoint? {
        val coords = geometry.optJSONArray("coordinates") ?: return null
        return when (geometry.optString("type")) {
            "Point" -> if (coords.length() >= 2) GeoPoint(coords.optDouble(1), coords.optDouble(0)) else null
            "LineString" -> {
                val p = coords.optJSONArray(coords.length() / 2) ?: return null
                if (p.length() >= 2) GeoPoint(p.optDouble(1), p.optDouble(0)) else null
            }
            else -> null
        }
    }

    private fun instant(value: String): Long? =
        value.takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
}
