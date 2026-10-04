package com.cosmicgrub.roadguard.domain

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.data.RoutingProvider
import com.cosmicgrub.roadguard.data.TollValidator
import com.cosmicgrub.roadguard.data.TollVerification

class RouteOrchestrator(
    private val routingProvider: RoutingProvider,
    private val tollValidator: TollValidator
) {
    suspend fun route(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy): RouteCandidate? {
        val ranked = routingProvider.candidates(origin, destination, policy)
            .filter { RouteEvaluator.score(it, policy).isFinite() }
            .sortedBy { RouteEvaluator.score(it, policy) }

        for (candidate in ranked) {
            if (policy.tollsAllowed) return candidate
            if (tollValidator.verifyZeroToll(candidate) == TollVerification.VerifiedZero) return candidate
        }
        return null
    }
}
