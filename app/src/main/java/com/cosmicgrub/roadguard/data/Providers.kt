package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent
import com.cosmicgrub.roadguard.domain.RouteCandidate
import com.cosmicgrub.roadguard.domain.RoutePolicy

data class GeoPoint(val latitude: Double, val longitude: Double)

interface RoutingProvider {
    suspend fun candidates(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy): List<RouteCandidate>
}

interface RoadEventProvider {
    suspend fun eventsAlong(corridor: List<GeoPoint>): List<RoadEvent>
}

interface TollValidator {
    suspend fun verifyZeroToll(route: RouteCandidate): TollVerification
}

sealed interface TollVerification {
    data object VerifiedZero : TollVerification
    data class TollDetected(val reason: String) : TollVerification
    data class Unknown(val reason: String) : TollVerification
}
