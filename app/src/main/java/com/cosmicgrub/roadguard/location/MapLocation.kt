package com.cosmicgrub.roadguard.location

import android.location.Location

object MapLocation {
    fun android(driver: DriverLocation): Location = Location("roadguard").apply {
        latitude = driver.point.latitude
        longitude = driver.point.longitude
        accuracy = driver.accuracyMeters
        driver.bearingDegrees?.let { bearing = it }
        driver.speedMetersPerSecond?.let { speed = it }
        time = driver.timestampEpochMs
    }
}
