package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.navigation.RouteOption

/** Evidence must be independent of the route-generating provider. */
interface IndependentTollEvidence {
    suspend fun verify(geometry: List<GeoPoint>): TollVerification
}

class IndependentTollRouteProvider(
    private val routing: NavigationRouteProvider,
    private val evidence: IndependentTollEvidence
) : NavigationRouteProvider {
    override suspend fun routes(
        origin: GeoPoint,
        destination: GeoPoint,
        policy: com.cosmicgrub.roadguard.domain.RoutePolicy
    ): List<RouteOption> = routing.routes(origin, destination, policy).map { option ->
        if (policy.tollsAllowed || option.candidate.tollSegments > 0 || option.geometry.size < 2) {
            return@map option.copy(zeroTollVerified = false)
        }
        val result = try {
            evidence.verify(option.geometry)
        } catch (cancel: kotlinx.coroutines.CancellationException) {
            throw cancel
        } catch (failure: Exception) {
            TollVerification.Unknown("Independent toll evidence unavailable")
        }
        option.copy(zeroTollVerified = result is TollVerification.VerifiedZero)
    }
}
