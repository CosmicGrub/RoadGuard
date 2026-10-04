package com.cosmicgrub.roadguard.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

enum class LocationAccess { NONE, APPROXIMATE, PRECISE }

fun Context.locationAccess(): LocationAccess = when {
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> LocationAccess.PRECISE
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> LocationAccess.APPROXIMATE
    else -> LocationAccess.NONE
}
