package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEventType
import org.junit.Assert.*
import org.junit.Test

class NwsAlertParserTest {
    @Test fun parsesSevereFloodAlert() {
        val json = """{"features":[{"id":"a1","geometry":{"type":"Polygon","coordinates":[[[-97.1,31.0],[-97.0,31.0],[-97.0,31.1],[-97.1,31.1],[-97.1,31.0]]]},"properties":{"id":"urn:alert:1","status":"Actual","event":"Flash Flood Warning","severity":"Severe","certainty":"Observed","effective":"2026-10-04T12:00:00Z","expires":"2026-10-04T14:00:00Z"}}]}"""
        val e = NwsAlertParser.events(json, "NWS", 1000).single()
        assertEquals(RoadEventType.FLOODING, e.type)
        assertEquals(4, e.severity)
        assertEquals(1.0, e.confidence, 0.0)
        assertEquals("NWS", e.source)
    }

    @Test fun mapsWinterAndIceHazards() {
        fun type(name: String) = NwsAlertParser.events("""{"features":[{"geometry":{"type":"Point","coordinates":[-97,31]},"properties":{"status":"Actual","event":"$name","severity":"Moderate","certainty":"Likely"}}]}""","NWS",1000).single().type
        assertEquals(RoadEventType.SNOW, type("Winter Storm Warning"))
        assertEquals(RoadEventType.ICE, type("Freezing Rain Advisory"))
    }

    @Test fun ignoresNonDrivingWeather() {
        val json = """{"features":[{"geometry":{"type":"Point","coordinates":[-97,31]},"properties":{"status":"Actual","event":"Heat Advisory","severity":"Moderate","certainty":"Likely"}}]}"""
        assertTrue(NwsAlertParser.events(json, "NWS", 1000).isEmpty())
    }
}
