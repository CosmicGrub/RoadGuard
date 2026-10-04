package com.cosmicgrub.roadguard.data

import org.junit.Assert.*
import org.junit.Test

class HerePolicyTest {
    @Test fun tollAvoidViolationIsDetected() {
        assertTrue(HerePolicy.providerReportsToll(listOf("avoidTollRoad")))
    }

    @Test fun unrelatedNoticeDoesNotBecomeTollViolation() {
        assertFalse(HerePolicy.providerReportsToll(listOf("seasonalClosure")))
    }
}
