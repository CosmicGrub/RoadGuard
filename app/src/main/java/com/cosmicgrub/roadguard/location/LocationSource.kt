package com.cosmicgrub.roadguard.location

import com.cosmicgrub.roadguard.data.GeoPoint
import kotlinx.coroutines.flow.Flow

data class DriverLocation(
    val point: GeoPoint,
    val accuracyMeters: Float,
    val bearingDegrees: Float? = null,
    val speedMetersPerSecond: Float? = null,
    val timestampEpochMs: Long
)

interface LocationSource {
    val locations: Flow<DriverLocation>
    suspend fun start()
    suspend fun stop()
}
