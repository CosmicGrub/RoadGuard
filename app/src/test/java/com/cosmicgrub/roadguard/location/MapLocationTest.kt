package com.cosmicgrub.roadguard.location

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class MapLocationTest {
    @Test fun driverLocationRetainsMapCoordinates() {
        val source = DriverLocation(GeoPoint(31.1,-97.7), 5f, 90f, 12f, 1234L)
        assertEquals(31.1, source.point.latitude, 0.0001)
        assertEquals(-97.7, source.point.longitude, 0.0001)
        assertEquals(1234L, source.timestampEpochMs)
    }
}
