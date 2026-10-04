package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent

interface BoundedRoadEventProvider {
    suspend fun events(bounds: GeoBounds): List<RoadEvent>
}

data class GeoBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double
) {
    companion object {
        fun around(points: List<GeoPoint>, paddingDegrees: Double = 0.02): GeoBounds? {
            if (points.isEmpty()) return null
            return GeoBounds(
                south = points.minOf { it.latitude } - paddingDegrees,
                west = points.minOf { it.longitude } - paddingDegrees,
                north = points.maxOf { it.latitude } + paddingDegrees,
                east = points.maxOf { it.longitude } + paddingDegrees
            )
        }
    }
}
