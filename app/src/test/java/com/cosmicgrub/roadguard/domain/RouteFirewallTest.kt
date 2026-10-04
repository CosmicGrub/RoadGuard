package com.cosmicgrub.roadguard.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteFirewallTest {
    @Test fun tollCandidateIsBlocked() {
        val route = RouteCandidate("a", 1.0, 1.0, 2, false, 0, 0, 0.0)
        assertEquals(FirewallStatus.BLOCKED, RouteFirewall.inspect(route, RoutePolicy()).first().status)
    }

    @Test fun zeroReportedTollsStillNeedsIndependentVerification() {
        val route = RouteCandidate("a", 1.0, 1.0, 0, false, 0, 0, 0.0)
        assertEquals(FirewallStatus.WARNING, RouteFirewall.inspect(route, RoutePolicy()).first().status)
    }
}
