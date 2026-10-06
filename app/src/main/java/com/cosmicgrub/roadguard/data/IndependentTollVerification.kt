package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.FirewallCheck
import com.cosmicgrub.roadguard.domain.FirewallStatus
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.RouteOption
import kotlinx.coroutines.CancellationException

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
        policy: RoutePolicy
    ): List<RouteOption> = routing.routes(origin, destination, policy).map { option ->
        if (policy.tollsAllowed || option.candidate.tollSegments > 0 || option.geometry.size < 2) {
            return@map option.copy(zeroTollVerified = false)
        }

        val result = try {
            evidence.verify(option.geometry)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (failure: Exception) {
            TollVerification.Unknown("Independent toll evidence unavailable")
        }

        if (result !is TollVerification.VerifiedZero) {
            return@map option.copy(zeroTollVerified = false)
        }

        option.copy(
            zeroTollVerified = true,
            firewall = option.firewall.map { check ->
                if (check.label == "Tolls") {
                    FirewallCheck("Tolls", FirewallStatus.VERIFIED, "Independently verified zero tolls")
                } else {
                    check
                }
            }
        )
    }
}
