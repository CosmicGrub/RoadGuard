package com.cosmicgrub.roadguard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TollGuruEvidenceTest {
    private val client = object : TollGuruHttpClient {
        override suspend fun verifyPolyline(url: String, apiKey: String, polyline: String) = "{}"
    }
    private val evidence = TollGuruEvidence(TollGuruConfig("test"), client)

    @Test fun parsesVerifiedZero() {
        assertEquals(TollVerification.VerifiedZero, evidence.parse("{\"route\":{\"hasTolls\":false}}"))
    }

    @Test fun parsesDetectedToll() {
        assertTrue(evidence.parse("{\"route\":{\"hasTolls\":true}}") is TollVerification.TollDetected)
    }

    @Test fun missingTollStatusFailsClosed() {
        assertTrue(evidence.parse("{\"route\":{}}") is TollVerification.Unknown)
    }

    @Test fun providerErrorFailsClosed() {
        assertTrue(evidence.parse("{\"status\":\"error\",\"message\":\"bad request\"}") is TollVerification.Unknown)
    }

    @Test fun encodesKnownPolylineExample() {
        val encoded = GooglePolyline.encode(listOf(GeoPoint(38.5, -120.2), GeoPoint(40.7, -120.95), GeoPoint(43.252, -126.453)))
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", encoded)
    }
}
