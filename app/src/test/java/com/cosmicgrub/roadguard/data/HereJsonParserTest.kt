package com.cosmicgrub.roadguard.data

import org.junit.Assert.*
import org.junit.Test

class HereJsonParserTest {
    @Test fun parsesDiscoverResult() {
        val json = """{"items":[{"id":"x","title":"Fort Cavazos","address":{"label":"Fort Cavazos, TX"},"position":{"lat":31.134,"lng":-97.78}}]}"""
        val place = HereJsonParser.places(json).single()
        assertEquals("Fort Cavazos", place.name)
        assertEquals(31.134, place.latitude, 0.0001)
    }

    @Test fun aggregatesRouteAndDetectsTollAvoidNotice() {
        val json = """{"routes":[{"id":"r1","sections":[{"summary":{"length":1000,"duration":60},"polyline":"BFoz5xJ67i1B1B7PzIhaxL7Y","notices":[{"code":"avoidTollRoad"}]}]}]}"""
        val route = HereJsonParser.routeSections(json).single()
        assertEquals(1000.0, route.distanceMeters, 0.01)
        assertEquals(60.0, route.durationSeconds, 0.01)
        assertEquals(4, route.geometry.size)
        assertTrue(HerePolicy.providerReportsToll(route.noticeCodes))
    }
}
