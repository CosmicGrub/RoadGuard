package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.domain.FirewallStatus
import kotlin.math.roundToInt

data class RouteCardModel(
    val id: String,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val verifiedZeroToll: Boolean,
    val warnings: Int,
    val blocked: Int
)

object RoutePresentation {
    fun card(option: RouteOption): RouteCardModel = RouteCardModel(
        id = option.candidate.id,
        durationMinutes = (option.candidate.durationSeconds / 60.0).roundToInt(),
        distanceMiles = option.candidate.distanceMeters / 1609.344,
        verifiedZeroToll = option.zeroTollVerified,
        warnings = option.firewall.count { it.status == FirewallStatus.WARNING },
        blocked = option.firewall.count { it.status == FirewallStatus.BLOCKED }
    )
}
