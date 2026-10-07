package com.cosmicgrub.roadguard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadGuardBackendTollEvidenceTest {
    private val evidence = RoadGuardBackendTollEvidence(RoadGuardTollVerifierConfig("https://example.com/tolls"))

    @Test fun parsesVerifiedZero() {
        assertEquals(TollVerification.VerifiedZero, evidence.parse("{\"status\":\"verified_zero\"}"))
    }

    @Test fun parsesDetectedToll() {
        assertTrue(evidence.parse("{\"status\":\"toll_detected\"}") is TollVerification.TollDetected)
    }

    @Test fun unknownStatusFailsClosed() {
        assertTrue(evidence.parse("{\"status\":\"unknown\"}") is TollVerification.Unknown)
    }

    @Test(expected = IllegalArgumentException::class)
    fun endpointMustUseHttps() {
        RoadGuardTollVerifierConfig("http://example.com/tolls")
    }
}
