package com.cosmicgrub.roadguard.domain

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class RouteEventMatcherTest {
    private fun event(id: String, lat: Double, lon: Double, expires: Long? = null) = RoadEvent(
        id, RoadEventType.CONSTRUCTION, lat, lon, 3, 1.0, "test", 0, 0, expires
    )

    @Test fun matchesNearbyAndRejectsDistantEvents() {
        val route = listOf(GeoPoint(31.0, -97.0), GeoPoint(31.01, -97.0))
        val impacts = RouteEventMatcher.impacts(route, listOf(
            event("near", 31.005, -97.0005),
            event("far", 31.005, -97.1)
        ), corridorMeters = 250.0, nowEpochMs = 1000)
        assertEquals(listOf("near"), impacts.map { it.event.id })
    }

    @Test fun expiredEventsDoNotAffectRoute() {
        val route = listOf(GeoPoint(31.0, -97.0), GeoPoint(31.01, -97.0))
        assertTrue(RouteEventMatcher.impacts(route, listOf(event("old", 31.005, -97.0, 999)), nowEpochMs = 1000).isEmpty())
    }
}
