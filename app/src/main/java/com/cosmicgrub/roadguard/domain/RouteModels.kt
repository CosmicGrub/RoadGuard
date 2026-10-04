package com.cosmicgrub.roadguard.domain

enum class RoadEventType {
    CRASH, CONSTRUCTION, ROAD_CLOSURE, LANE_CLOSURE, POLICE_REPORTED,
    FLOODING, SNOW, ICE, SEVERE_WEATHER, DISABLED_VEHICLE, DEBRIS,
    PUBLIC_ALPR, SPEED_CAMERA, RED_LIGHT_CAMERA, TOLL_POINT
}

data class RoadEvent(
    val id: String,
    val type: RoadEventType,
    val latitude: Double,
    val longitude: Double,
    val severity: Int,
    val confidence: Double,
    val source: String,
    val firstSeenEpochMs: Long,
    val lastVerifiedEpochMs: Long,
    val expiresAtEpochMs: Long? = null
)

data class RoutePolicy(
    val tollsAllowed: Boolean = false,
    val avoidSevereWeather: Boolean = true,
    val avoidConstruction: Boolean = true,
    val avoidClosures: Boolean = true,
    val trafficWeight: Double = 1.0,
    val weatherWeight: Double = 1.5,
    val constructionWeight: Double = 1.2
)

data class RouteCandidate(
    val id: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val tollSegments: Int,
    val hasClosure: Boolean,
    val constructionDelaySeconds: Int,
    val trafficDelaySeconds: Int,
    val weatherRisk: Double
)
