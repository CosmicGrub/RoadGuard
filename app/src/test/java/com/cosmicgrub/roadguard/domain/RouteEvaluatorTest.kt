package com.cosmicgrub.roadguard.domain

import org.junit.Assert.*
import org.junit.Test

class RouteEvaluatorTest {
    private val clean = RouteCandidate("clean", 1000.0, 600.0, 0, false, 0, 0, 0.0)

    @Test fun zeroTollPolicyRejectsTollRoute() {
        val toll = clean.copy(id = "toll", tollSegments = 1, durationSeconds = 300.0)
        assertTrue(RouteEvaluator.score(toll, RoutePolicy(tollsAllowed = false)).isInfinite())
        assertEquals("clean", RouteEvaluator.best(listOf(toll, clean), RoutePolicy())?.id)
    }

    @Test fun closureIsHardBlockedByDefault() {
        assertTrue(RouteEvaluator.score(clean.copy(hasClosure = true), RoutePolicy()).isInfinite())
    }

    @Test fun lowerRiskRouteCanBeatFasterRiskyRoute() {
        val risky = clean.copy(id = "risky", durationSeconds = 400.0, weatherRisk = 1.0)
        assertEquals("clean", RouteEvaluator.best(listOf(risky, clean), RoutePolicy())?.id)
    }
}
