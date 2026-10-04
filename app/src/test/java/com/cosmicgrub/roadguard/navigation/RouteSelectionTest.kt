package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.domain.RouteCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSelectionTest {
    private fun option(id: String, seconds: Double) =
        RouteOption(RouteCandidate(id, 1.0, seconds, 0, false, 0, 0, 0.0), emptyList(), emptyList(), true)

    @Test fun defaultsToFastestEligibleRoute() {
        assertEquals("b", RouteSelection.select(listOf(option("a", 100.0), option("b", 50.0)), null)?.candidate?.id)
    }

    @Test fun respectsExplicitSelection() {
        assertEquals("a", RouteSelection.select(listOf(option("a", 100.0), option("b", 50.0)), "a")?.candidate?.id)
    }
}
