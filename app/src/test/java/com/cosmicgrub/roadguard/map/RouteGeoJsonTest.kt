package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteGeoJsonTest {
    @Test fun coordinatesAreLongitudeLatitude() {
        val result = RouteGeoJson.collection(listOf(RouteLine("r", listOf(
            GeoPoint(31.1, -97.7), GeoPoint(31.2, -97.6)
        ), true)))
        val line = result.features()!!.single().geometry() as org.maplibre.geojson.LineString
        assertEquals(-97.7, line.coordinates().first().longitude(), 0.0001)
        assertEquals(31.1, line.coordinates().first().latitude(), 0.0001)
    }
}
