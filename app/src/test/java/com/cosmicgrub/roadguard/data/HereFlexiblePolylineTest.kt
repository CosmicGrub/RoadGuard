package com.cosmicgrub.roadguard.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HereFlexiblePolylineTest {
    @Test fun decodesOfficialTwoDimensionalExample() {
        val points = HereFlexiblePolyline.decode("BFoz5xJ67i1B1B7PzIhaxL7Y")
        assertEquals(4, points.size)
        assertEquals(50.10228, points[0].latitude, 0.00001)
        assertEquals(8.69821, points[0].longitude, 0.00001)
    }
}
