package com.cosmicgrub.roadguard.location

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest

@Composable
fun rememberDriverLocation(enabled: Boolean): DriverLocation? {
    val context = LocalContext.current
    val source = remember { AndroidLocationSource(context.applicationContext) }
    var location by remember { mutableStateOf<DriverLocation?>(null) }

    LaunchedEffect(source, enabled) {
        if (!enabled) return@LaunchedEffect
        source.start()
        try {
            source.locations.collectLatest { location = it }
        } finally {
            source.stop()
        }
    }
    return location
}
