package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class MapCameraBoundsTest {
    @Test fun computesBounds() {
        val b = MapCameraBounds.forPoints(listOf(GeoPoint(31.0,-98.0), GeoPoint(32.0,-97.0)))!!
        assertEquals(31.0, b.southWest.latitude, 0.0)
        assertEquals(-98.0, b.southWest.longitude, 0.0)
        assertEquals(32.0, b.northEast.latitude, 0.0)
        assertEquals(-97.0, b.northEast.longitude, 0.0)
    }
}
