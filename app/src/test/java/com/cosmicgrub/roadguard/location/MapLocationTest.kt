package com.cosmicgrub.roadguard.location

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class MapLocationTest {
    @Test fun preservesDriverLocation() {
        val source = DriverLocation(GeoPoint(31.1,-97.7), 5f, 90f, 12f, 1234L)
        val location = MapLocation.android(source)
        assertEquals(31.1, location.latitude, 0.0001)
        assertEquals(-97.7, location.longitude, 0.0001)
        assertEquals(1234L, location.time)
    }
}
