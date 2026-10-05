package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEventType
import org.junit.Assert.*
import org.junit.Test

class WzdxParserTest {
    @Test fun parsesActiveLineStringWorkZone() {
        val json = """{"features":[{"id":"w1","geometry":{"type":"LineString","coordinates":[[-97.0,31.0],[-97.01,31.01],[-97.02,31.02]]},"properties":{"road_event_type":"work-zone","core_details":{"event_status":"active","description":"lane work","start_date":"2026-10-04T12:00:00Z","end_date":"2026-10-05T12:00:00Z"}}}]}"""
        val e = WzdxParser.events(json, "DOT", 1000).single()
        assertEquals("w1", e.id)
        assertEquals(RoadEventType.CONSTRUCTION, e.type)
        assertEquals(31.01, e.latitude, 0.0001)
        assertEquals(-97.01, e.longitude, 0.0001)
        assertEquals("DOT", e.source)
        assertEquals(1000, e.lastVerifiedEpochMs)
    }

    @Test fun ignoresCompletedEvents() {
        val json = """{"features":[{"id":"old","geometry":{"type":"Point","coordinates":[-97.0,31.0]},"properties":{"core_details":{"event_status":"completed"}}}]}"""
        assertTrue(WzdxParser.events(json, "DOT", 1000).isEmpty())
    }

    @Test fun classifiesClosedDescriptionAsClosure() {
        val json = """{"features":[{"id":"c","geometry":{"type":"Point","coordinates":[-97.0,31.0]},"properties":{"core_details":{"event_status":"active","description":"Road closed for bridge work"}}}]}"""
        assertEquals(RoadEventType.ROAD_CLOSURE, WzdxParser.events(json, "DOT", 1000).single().type)
    }
}
