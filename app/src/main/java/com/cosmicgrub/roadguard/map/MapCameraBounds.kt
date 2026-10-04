package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint

data class GeoBounds(val southWest: GeoPoint, val northEast: GeoPoint)

object MapCameraBounds {
    fun forPoints(points: List<GeoPoint>): GeoBounds? {
        if (points.isEmpty()) return null
        return GeoBounds(
            southWest = GeoPoint(points.minOf { it.latitude }, points.minOf { it.longitude }),
            northEast = GeoPoint(points.maxOf { it.latitude }, points.maxOf { it.longitude })
        )
    }
}
