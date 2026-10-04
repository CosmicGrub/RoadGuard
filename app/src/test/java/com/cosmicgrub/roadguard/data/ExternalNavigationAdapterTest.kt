package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoutePolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExternalNavigationAdapterTest {
    private val origin = GeoPoint(31.0, -97.0)
    private val destination = GeoPoint(32.0, -96.0)

    @Test fun hardTollPolicyIsForwardedAndProviderClaimIsNotIndependentVerification() = runBlocking {
        var requestedAvoidTolls: Boolean? = null
        val service = object : ExternalNavigationService {
            override suspend fun search(query: String, near: GeoPoint?) = emptyList<ProviderPlace>()

            override suspend fun routes(
                origin: GeoPoint,
                destination: GeoPoint,
                avoidTolls: Boolean
            ): List<ProviderRoute> {
                requestedAvoidTolls = avoidTolls
                return listOf(
                    ProviderRoute("r", 1000.0, 60.0, listOf(origin, destination), providerReportsToll = false)
                )
            }
        }

        val routes = ExternalNavigationAdapter(service)
            .routes(origin, destination, RoutePolicy(tollsAllowed = false))

        assertEquals(true, requestedAvoidTolls)
        assertFalse(routes.single().zeroTollVerified)
    }
}
