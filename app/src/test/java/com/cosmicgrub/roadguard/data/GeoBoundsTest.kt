package com.cosmicgrub.roadguard.data

import org.junit.Assert.*
import org.junit.Test

class GeoBoundsTest {
    @Test fun boundsCoverRouteWithPadding() {
        val b = GeoBounds.around(listOf(GeoPoint(31.0,-97.0), GeoPoint(32.0,-96.0)), 0.1)!!
        assertEquals(30.9, b.south, 0.0001)
        assertEquals(-97.1, b.west, 0.0001)
        assertEquals(32.1, b.north, 0.0001)
        assertEquals(-95.9, b.east, 0.0001)
    }
}
