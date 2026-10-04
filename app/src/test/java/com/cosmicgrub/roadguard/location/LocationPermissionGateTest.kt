package com.cosmicgrub.roadguard.location

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationPermissionGateTest {
    @Test fun preciseWinsWhenBothGranted() {
        assertEquals(LocationAccess.PRECISE, LocationPermissionGate.access(mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to true,
            Manifest.permission.ACCESS_COARSE_LOCATION to true
        )))
    }

    @Test fun approximateIsSupported() {
        assertEquals(LocationAccess.APPROXIMATE, LocationPermissionGate.access(mapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to false,
            Manifest.permission.ACCESS_COARSE_LOCATION to true
        )))
    }

    @Test fun denialProducesNone() {
        assertEquals(LocationAccess.NONE, LocationPermissionGate.access(emptyMap()))
    }
}
