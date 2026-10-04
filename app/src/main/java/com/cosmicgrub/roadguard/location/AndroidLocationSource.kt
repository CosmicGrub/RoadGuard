package com.cosmicgrub.roadguard.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.cosmicgrub.roadguard.data.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AndroidLocationSource(private val context: Context) : LocationSource, LocationListener {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val updates = MutableSharedFlow<DriverLocation>(replay = 1, extraBufferCapacity = 8)
    override val locations: Flow<DriverLocation> = updates

    override suspend fun start() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return
        val provider = when {
            fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }
        manager.getLastKnownLocation(provider)?.let(::publish)
        manager.requestLocationUpdates(provider, 1_000L, 3f, this, context.mainLooper)
    }

    override suspend fun stop() = manager.removeUpdates(this)
    override fun onLocationChanged(location: Location) = publish(location)
    @Deprecated("Deprecated by Android")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit

    private fun publish(location: Location) {
        updates.tryEmit(DriverLocation(
            GeoPoint(location.latitude, location.longitude),
            location.accuracy,
            location.bearing.takeIf { location.hasBearing() },
            location.speed.takeIf { location.hasSpeed() },
            location.time
        ))
    }
}
