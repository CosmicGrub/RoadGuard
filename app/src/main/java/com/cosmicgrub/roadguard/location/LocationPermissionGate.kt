package com.cosmicgrub.roadguard.location

import android.Manifest

object LocationPermissionGate {
    val foregroundPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun access(grants: Map<String, Boolean>): LocationAccess = when {
        grants[Manifest.permission.ACCESS_FINE_LOCATION] == true -> LocationAccess.PRECISE
        grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> LocationAccess.APPROXIMATE
        else -> LocationAccess.NONE
    }
}
