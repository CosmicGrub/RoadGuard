package com.cosmicgrub.roadguard.domain

import com.cosmicgrub.roadguard.data.GeoPoint
import kotlin.math.*

data class RouteEventImpact(
    val event: RoadEvent,
    val distanceToRouteMeters: Double
)

object RouteEventMatcher {
    fun impacts(
        geometry: List<GeoPoint>,
        events: List<RoadEvent>,
        corridorMeters: Double = 250.0,
        nowEpochMs: Long = System.currentTimeMillis()
    ): List<RouteEventImpact> {
        if (geometry.isEmpty()) return emptyList()
        return events.asSequence()
            .filter { it.expiresAtEpochMs == null || it.expiresAtEpochMs > nowEpochMs }
            .map { event ->
                val point = GeoPoint(event.latitude, event.longitude)
                RouteEventImpact(event, distanceToPolylineMeters(point, geometry))
            }
            .filter { it.distanceToRouteMeters <= corridorMeters }
            .sortedBy { it.distanceToRouteMeters }
            .toList()
    }

    private fun distanceToPolylineMeters(point: GeoPoint, line: List<GeoPoint>): Double {
        if (line.size == 1) return haversineMeters(point, line.first())
        return line.zipWithNext().minOf { (a, b) -> segmentDistanceMeters(point, a, b) }
    }

    private fun segmentDistanceMeters(p: GeoPoint, a: GeoPoint, b: GeoPoint): Double {
        val lat0 = Math.toRadians(p.latitude)
        fun xy(q: GeoPoint): Pair<Double, Double> {
            val x = Math.toRadians(q.longitude - p.longitude) * cos(lat0) * EARTH_RADIUS_M
            val y = Math.toRadians(q.latitude - p.latitude) * EARTH_RADIUS_M
            return x to y
        }
        val (ax, ay) = xy(a); val (bx, by) = xy(b)
        val dx = bx - ax; val dy = by - ay
        val t = if (dx == 0.0 && dy == 0.0) 0.0 else (-(ax * dx + ay * dy) / (dx * dx + dy * dy)).coerceIn(0.0, 1.0)
        return hypot(ax + t * dx, ay + t * dy)
    }

    private fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude); val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_M * asin(sqrt(h))
    }

    private const val EARTH_RADIUS_M = 6_371_000.0
}
