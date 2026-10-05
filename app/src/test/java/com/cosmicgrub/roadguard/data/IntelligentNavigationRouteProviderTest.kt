package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.*
import com.cosmicgrub.roadguard.navigation.RouteOption
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class IntelligentNavigationRouteProviderTest {
    private val policy = RoutePolicy(tollsAllowed = true)

    @Test fun closureEnrichesRouteAndRebuildsFirewall() = runBlocking {
        val base = object : NavigationRouteProvider {
            override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy) = listOf(
                RouteOption(
                    RouteCandidate("r", 1000.0, 60.0, 0, false, 0, 0, 0.0),
                    listOf(GeoPoint(31.0,-97.0), GeoPoint(31.01,-97.0)),
                    emptyList(), true
                )
            )
        }
        val events = object : BoundedRoadEventProvider {
            override suspend fun events(bounds: GeoBounds) = listOf(
                RoadEvent("c", RoadEventType.ROAD_CLOSURE,31.005,-97.0,5,1.0,"DOT",0,1000,null)
            )
        }
        val route = IntelligentNavigationRouteProvider(base,listOf(events)){1000}
            .routes(GeoPoint(31.0,-97.0),GeoPoint(31.01,-97.0),policy).single()
        assertTrue(route.candidate.hasClosure)
        assertTrue(route.firewall.any { it.label == "Closures" && it.status == FirewallStatus.BLOCKED })
    }

    @Test fun providerFailureDoesNotEraseRoute() = runBlocking {
        val candidate = RouteCandidate("r",1000.0,60.0,0,false,0,0,0.0)
        val base = object : NavigationRouteProvider {
            override suspend fun routes(origin: GeoPoint,destination: GeoPoint,policy: RoutePolicy) =
                listOf(RouteOption(candidate,listOf(origin,destination),emptyList(),true))
        }
        val failed = object : BoundedRoadEventProvider {
            override suspend fun events(bounds: GeoBounds): List<RoadEvent> = error("feed unavailable")
        }
        val route = IntelligentNavigationRouteProvider(base,listOf(failed)){1000}
            .routes(GeoPoint(31.0,-97.0),GeoPoint(31.01,-97.0),policy).single()
        assertEquals(candidate, route.candidate)
    }
}
