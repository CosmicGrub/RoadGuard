package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.domain.*
import org.junit.Assert.*
import org.junit.Test

class RoutePresentationTest {
    @Test fun exposesVerifiedTollStateAndWarnings() {
        val candidate = RouteCandidate("safe", 1609.344, 600.0, 0, false, 0, 0, 0.0)
        val option = RouteOption(candidate, emptyList(), listOf(
            FirewallCheck("Weather", FirewallStatus.WARNING, "Watch"),
            FirewallCheck("Closures", FirewallStatus.VERIFIED, "Clear")
        ), true)
        val card = RoutePresentation.card(option)
        assertEquals(10, card.durationMinutes)
        assertEquals(1.0, card.distanceMiles, 0.001)
        assertTrue(card.verifiedZeroToll)
        assertEquals(1, card.warnings)
        assertEquals(0, card.blocked)
    }
}
