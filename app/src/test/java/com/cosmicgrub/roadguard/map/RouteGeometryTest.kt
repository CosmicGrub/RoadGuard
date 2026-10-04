package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.domain.RouteCandidate
import com.cosmicgrub.roadguard.navigation.RouteOption
import org.junit.Assert.*
import org.junit.Test

class RouteGeometryTest {
    private fun option(id: String, points: List<GeoPoint>) =
        RouteOption(RouteCandidate(id, 1.0, 1.0, 0, false, 0, 0, 0.0), points, emptyList(), true)

    @Test fun onlyRenderableGeometryBecomesLines() {
        val a = option("a", listOf(GeoPoint(1.0, 1.0), GeoPoint(2.0, 2.0)))
        val b = option("b", listOf(GeoPoint(1.0, 1.0)))
        val lines = RouteGeometry.lines(listOf(a, b), "a")
        assertEquals(1, lines.size)
        assertTrue(lines.single().selected)
    }
}
