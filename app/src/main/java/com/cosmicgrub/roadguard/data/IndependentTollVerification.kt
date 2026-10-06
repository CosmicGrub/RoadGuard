package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.FirewallCheck
import com.cosmicgrub.roadguard.domain.FirewallStatus
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.RouteOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
    ): List<RouteOption> = coroutineScope {
        routing.routes(origin, destination, policy).map { option ->
            async { verify(option, policy) }
        }.awaitAll()
    }

    private suspend fun verify(option: RouteOption, policy: RoutePolicy): RouteOption {
        if (policy.tollsAllowed || option.candidate.tollSegments > 0 || option.geometry.size < 2) {
            return option.copy(zeroTollVerified = false)
        }

        val result = try {
            evidence.verify(option.geometry)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (failure: Exception) {
            TollVerification.Unknown("Independent toll evidence unavailable")
        }

        return when (result) {
            TollVerification.VerifiedZero -> option.copy(
                zeroTollVerified = true,
                firewall = option.firewall.map { check ->
                    if (check.label == "Tolls") {
                        FirewallCheck("Tolls", FirewallStatus.VERIFIED, "Independently verified zero tolls")
                    } else check
                }
            )
            is TollVerification.TollDetected -> option.copy(
                zeroTollVerified = false,
                candidate = option.candidate.copy(
                    tollSegments = maxOf(1, option.candidate.tollSegments)
                ),
                firewall = option.firewall.map { check ->
                    if (check.label == "Tolls") {
                        FirewallCheck("Tolls", FirewallStatus.BLOCKED, result.reason)
                    } else check
                }
            )
            is TollVerification.Unknown -> option.copy(zeroTollVerified = false)
        }
    }
}
