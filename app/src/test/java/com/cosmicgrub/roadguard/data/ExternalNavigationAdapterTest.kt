package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoutePolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExternalNavigationAdapterTest {
    private val origin = GeoPoint(31.0, -97.0)
    private val destination = GeoPoint(32.0, -96.0)

    @Test fun hardTollPolicyIsForwardedAndProviderClaimIsNotIndependentVerification() = runBlocking {
        var avoidTolls: Boolean? = null
        val service = object : ExternalNavigationService {
            override suspend fun search(query: String, near: GeoPoint?) = emptyList<ProviderPlace>()
            override suspend fun routes(origin: GeoPoint, destination: GeoPoint, avoidTolls: Boolean): List<ProviderRoute> {
                this@ExternalNavigationAdapterTest
                avoidTolls.also { }
                return listOf(ProviderRoute("r", 1000.0, 60.0, listOf(origin, destination), false))
            }
        }
        val tracking = object : ExternalNavigationService {
            override suspend fun search(query: String, near: GeoPoint?) = service.search(query, near)
            override suspend fun routes(origin: GeoPoint, destination: GeoPoint, avoidTollsArg: Boolean): List<ProviderRoute> {
                avoidTolls = avoidTollsArg
                return service.routes(origin, destination, avoidTollsArg)
            }
        }
        val routes = ExternalNavigationAdapter(tracking).routes(origin, destination, RoutePolicy(tollsAllowed = false))
        assertEquals(true, avoidTolls)
        assertFalse(routes.single().zeroTollVerified)
    }
}
