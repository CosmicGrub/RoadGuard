package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.FirewallCheck
import com.cosmicgrub.roadguard.domain.FirewallStatus
import com.cosmicgrub.roadguard.domain.RouteCandidate
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.RouteOption
import kotlinx.coroutines.CompletableDeferred\nimport kotlinx.coroutines.async\nimport kotlinx.coroutines.runBlocking\nimport kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class IndependentTollRouteProviderTest {
    private val origin = GeoPoint(31.0, -97.0)
    private val destination = GeoPoint(31.1, -97.1)

    private fun provider(tollSegments: Int = 0) = object : NavigationRouteProvider {
        override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy) =
            listOf(
                RouteOption(
                    RouteCandidate("r", 1000.0, 100.0, tollSegments, false, 0, 0, 0.0),
                    listOf(origin, destination),
                    listOf(
                        FirewallCheck(
                            "Tolls",
                            if (tollSegments == 0) FirewallStatus.WARNING else FirewallStatus.BLOCKED,
                            if (tollSegments == 0) "Candidate reports zero; independent verification required"
                            else "$tollSegments toll segment(s)"
                        )
                    ),
                    false
                )
            )
    }

    @Test fun independentEvidenceCanVerifyAndClearsTollWarning() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.VerifiedZero
        }
        val result = IndependentTollRouteProvider(provider(), evidence)
            .routes(origin, destination, RoutePolicy()).single()

        assertTrue(result.zeroTollVerified)
        val tollCheck = result.firewall.single { it.label == "Tolls" }
        assertEquals(FirewallStatus.VERIFIED, tollCheck.status)
        assertEquals("Independently verified zero tolls", tollCheck.detail)
        assertFalse(result.firewall.any { it.label == "Tolls" && it.status == FirewallStatus.WARNING })
    }

    @Test fun unknownEvidenceNeverVerifiesAndKeepsWarning() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.Unknown("coverage gap")
        }
        val result = IndependentTollRouteProvider(provider(), evidence)
            .routes(origin, destination, RoutePolicy()).single()

        assertFalse(result.zeroTollVerified)
        assertEquals(FirewallStatus.WARNING, result.firewall.single { it.label == "Tolls" }.status)
    }

    @Test fun providerReportedTollCannotBeOverridden() = runBlocking {
        val evidence = object : IndependentTollEvidence {
            override suspend fun verify(geometry: List<GeoPoint>) = TollVerification.VerifiedZero
        }
        val result = IndependentTollRouteProvider(provider(1), evidence)
            .routes(origin, destination, RoutePolicy()).single()

        assertFalse(result.zeroTollVerified)
        assertEquals(FirewallStatus.BLOCKED, result.firewall.single { it.label == "Tolls" }.status)
    }
}
