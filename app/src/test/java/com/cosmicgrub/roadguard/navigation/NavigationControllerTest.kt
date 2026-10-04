package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.*
import com.cosmicgrub.roadguard.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationControllerTest {
    private val point = GeoPoint(31.0, -97.0)
    private val places = object : PlaceSearchProvider {
        override suspend fun search(query: String, near: GeoPoint?) =
            listOf(PlaceSuggestion("1", query, "result", point))
    }

    @Test fun rejectsRouteWithoutIndependentZeroTollVerification() = runBlocking {
        val routing = object : NavigationRouteProvider {
            override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy) =
                listOf(RouteOption(
                    RouteCandidate("fast", 1000.0, 60.0, 0, false, 0, 0, 0.0),
                    listOf(origin, destination),
                    RouteFirewall.inspect(RouteCandidate("fast", 1000.0, 60.0, 0, false, 0, 0, 0.0), policy),
                    zeroTollVerified = false
                ))
        }
        assertEquals(0, NavigationController(places, routing).route(point, point).size)
    }
}
