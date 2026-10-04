package com.cosmicgrub.roadguard.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cosmicgrub.roadguard.location.DriverLocation
import com.cosmicgrub.roadguard.navigation.NavigationUiState

@Composable
fun RoadGuardMap(
    state: NavigationUiState,
    driverLocation: DriverLocation?,
    modifier: Modifier = Modifier,
    config: MapLibreConfig = MapLibreConfig("https://demotiles.maplibre.org/style.json")
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val map = remember { MapLibreMapView(context, config) }

    DisposableEffect(lifecycleOwner, map) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> map.onStart()
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                Lifecycle.Event.ON_STOP -> map.onStop()
                Lifecycle.Event.ON_DESTROY -> map.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            map.onDestroy()
        }
    }

    AndroidView(
        factory = { map },
        modifier = modifier,
        update = {
            it.render(MapUiModel(state.routes, state.selectedRouteId, driverLocation?.point))
            driverLocation?.let(it::showDriverLocation)
        }
    )
}
