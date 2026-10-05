package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RouteCandidate
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.RouteOption
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class IndependentTollRouteProviderTest {
    private val origin = GeoPoint(31.0, -97.0)
    private val destination = GeoPoint(31.1, -97.1)
    private fun provider(tollSegments: Int = 0) = object : NavigationRouteProvider {
        override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy) =
            listOf(RouteOption(RouteCandidate("r", 1000.0, 100.0, tollSegments, false, 0, 0, 0.0),
                listOf(origin, destination), emptyList(), false))
    }
    @Test fun independentEvidenceCanVerify() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.VerifiedZero
        }
        val result = IndependentTollRouteProvider(provider(), evidence)
            .routes(origin, destination, RoutePolicy()).single()
        assertTrue(result.zeroTollVerified)
    }
    @Test fun unknownEvidenceNeverVerifies() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.Unknown("coverage gap")
        }
        assertFalse(IndependentTollRouteProvider(provider(), evidence)
            .routes(origin, destination, RoutePolicy()).single().zeroTollVerified)
    }
    @Test fun providerReportedTollCannotBeOverridden() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.VerifiedZero
        }
        assertFalse(IndependentTollRouteProvider(provider(1), evidence)
            .routes(origin, destination, RoutePolicy()).single().zeroTollVerified)
    }
}
