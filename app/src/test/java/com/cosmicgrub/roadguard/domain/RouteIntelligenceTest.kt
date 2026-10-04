package com.cosmicgrub.roadguard.domain

import com.cosmicgrub.roadguard.data.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class RouteIntelligenceTest {
    private fun event(id: String, type: RoadEventType, severity: Int = 5, confidence: Double = 1.0) =
        RoadEvent(id, type, 31.005, -97.0, severity, confidence, "test", 0, 0, null)

    @Test fun closureBecomesHardFirewallBlock() {
        val geometry = listOf(GeoPoint(31.0,-97.0), GeoPoint(31.01,-97.0))
        val summary = RouteIntelligence.summarize(geometry, listOf(event("c", RoadEventType.ROAD_CLOSURE)), 1000)
        val base = RouteCandidate("r",1000.0,60.0,0,false,0,0,0.0)
        val enriched = RouteIntelligence.apply(base, summary)
        assertTrue(enriched.hasClosure)
        assertTrue(RouteFirewall.inspect(enriched, RoutePolicy()).any { it.label == "Closures" && it.status == FirewallStatus.BLOCKED })
    }

    @Test fun weatherRiskUsesSeverityAndConfidence() {
        val geometry = listOf(GeoPoint(31.0,-97.0), GeoPoint(31.01,-97.0))
        val summary = RouteIntelligence.summarize(geometry, listOf(event("w", RoadEventType.SEVERE_WEATHER, 4, 0.5)), 1000)
        assertEquals(0.4, summary.weatherRisk, 0.0001)
    }
}
