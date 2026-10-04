package com.cosmicgrub.roadguard.data

import org.json.JSONObject

object HereJsonParser {
    fun places(json: String): List<ProviderPlace> {
        val items = JSONObject(json).optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val position = item.optJSONObject("position") ?: continue
                add(
                    ProviderPlace(
                        id = item.optString("id", "here-$i"),
                        name = item.optString("title", "Destination"),
                        label = item.optJSONObject("address")?.optString("label").orEmpty(),
                        latitude = position.getDouble("lat"),
                        longitude = position.getDouble("lng")
                    )
                )
            }
        }
    }

    fun routeSections(json: String): List<HereRouteSection> {
        val routes = JSONObject(json).optJSONArray("routes") ?: return emptyList()
        return buildList {
            for (i in 0 until routes.length()) {
                val route = routes.getJSONObject(i)
                val sections = route.optJSONArray("sections") ?: continue
                val decoded = mutableListOf<GeoPoint>()
                var distance = 0.0
                var duration = 0.0
                val notices = mutableListOf<String>()
                for (j in 0 until sections.length()) {
                    val section = sections.getJSONObject(j)
                    val summary = section.optJSONObject("summary")
                    distance += summary?.optDouble("length", 0.0) ?: 0.0
                    duration += summary?.optDouble("duration", 0.0) ?: 0.0
                    val polyline = section.optString("polyline")
                    if (polyline.isNotBlank()) decoded += HereFlexiblePolyline.decode(polyline)
                    val noticeArray = section.optJSONArray("notices")
                    if (noticeArray != null) for (k in 0 until noticeArray.length()) {
                        notices += noticeArray.getJSONObject(k).optString("code")
                    }
                }
                add(HereRouteSection(route.optString("id", "route-$i"), distance, duration, decoded, notices))
            }
        }
    }
}

data class HereRouteSection(
    val id: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val geometry: List<GeoPoint>,
    val noticeCodes: List<String>
)
