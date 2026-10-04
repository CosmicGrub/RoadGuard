package com.cosmicgrub.roadguard.domain

import com.cosmicgrub.roadguard.data.GeoPoint

data class RouteIntelligenceSummary(
    val closureCount: Int,
    val constructionCount: Int,
    val severeWeatherCount: Int,
    val weatherRisk: Double
)

object RouteIntelligence {
    fun summarize(geometry: List<GeoPoint>, events: List<RoadEvent>, nowEpochMs: Long): RouteIntelligenceSummary {
        val impacts = RouteEventMatcher.impacts(geometry, events, nowEpochMs = nowEpochMs)
        val closures = impacts.count { it.event.type == RoadEventType.ROAD_CLOSURE }
        val construction = impacts.count { it.event.type == RoadEventType.CONSTRUCTION || it.event.type == RoadEventType.LANE_CLOSURE }
        val weather = impacts.filter { it.event.type in WEATHER_TYPES }
        val risk = weather.maxOfOrNull {
            (it.event.severity.coerceIn(0, 5) / 5.0) * it.event.confidence.coerceIn(0.0, 1.0)
        } ?: 0.0
        return RouteIntelligenceSummary(closures, construction, weather.size, risk)
    }

    fun apply(candidate: RouteCandidate, summary: RouteIntelligenceSummary): RouteCandidate =
        candidate.copy(
            hasClosure = candidate.hasClosure || summary.closureCount > 0,
            constructionDelaySeconds = candidate.constructionDelaySeconds + summary.constructionCount * 120,
            weatherRisk = maxOf(candidate.weatherRisk, summary.weatherRisk)
        )

    private val WEATHER_TYPES = setOf(
        RoadEventType.FLOODING, RoadEventType.SNOW, RoadEventType.ICE, RoadEventType.SEVERE_WEATHER
    )
}
