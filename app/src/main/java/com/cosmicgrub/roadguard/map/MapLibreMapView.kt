package com.cosmicgrub.roadguard.map

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.cosmicgrub.roadguard.data.GeoPoint
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.RenderMode
import com.cosmicgrub.roadguard.location.DriverLocation
import com.cosmicgrub.roadguard.location.MapLocation
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.eq
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.switchCase
import org.maplibre.android.style.sources.GeoJsonSource

class MapLibreMapView(
    context: Context,
    private val config: MapLibreConfig
) : FrameLayout(context), MapRenderer {
    private val mapView = MapView(context)
    private var latest = MapUiModel()

    init {
        addView(mapView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri(config.styleUri)) { style ->
                ensureRouteLayers(style)
                render(latest)
            }
        }
    }

    override fun render(model: MapUiModel) {
        latest = model
        mapView.getMapAsync { map ->
            val style = map.style ?: return@getMapAsync
            ensureRouteLayers(style)
            val lines = RouteGeometry.lines(model.routes, model.selectedRouteId)
            style.getSourceAs<GeoJsonSource>(ROUTES_SOURCE)?.setGeoJson(RouteGeoJson.collection(lines))

            val selected = model.routes.firstOrNull { it.candidate.id == model.selectedRouteId }
            val points = selected?.geometry.orEmpty()
            MapCameraBounds.forPoints(points)?.let { bounds ->
                map.easeCamera(CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder()
                        .include(LatLng(bounds.southWest.latitude, bounds.southWest.longitude))
                        .include(LatLng(bounds.northEast.latitude, bounds.northEast.longitude))
                        .build(),
                    96
                ))
            } ?: model.userLocation?.let { map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0)
            ) }
        }
    }

    fun showDriverLocation(location: DriverLocation) {
        mapView.getMapAsync { map ->
            val style = map.style ?: return@getMapAsync
            val component = map.locationComponent
            if (!component.isLocationComponentActivated) {
                component.activateLocationComponent(
                    LocationComponentActivationOptions.builder(context, style)
                        .useDefaultLocationEngine(false)
                        .useSpecializedLocationLayer(true)
                        .build()
                )
                component.isLocationComponentEnabled = true
                component.renderMode = RenderMode.COMPASS
            }
            component.forceLocationUpdate(MapLocation.android(location))
        }
    }

    fun nativeView(): View = this

    private fun ensureRouteLayers(style: Style) {
        if (style.getSource(ROUTES_SOURCE) == null) style.addSource(GeoJsonSource(ROUTES_SOURCE))
        if (style.getLayer(ROUTES_LAYER) == null) {
            style.addLayer(LineLayer(ROUTES_LAYER, ROUTES_SOURCE).withProperties(
                lineColor(switchCase(eq(get("selected"), literal(true)), literal(Color.rgb(30, 100, 230)), literal(Color.rgb(120, 120, 120)))),
                lineWidth(switchCase(eq(get("selected"), literal(true)), literal(7f), literal(4f)))
            ))
        }
    }

    fun onStart() = mapView.onStart()
    fun onResume() = mapView.onResume()
    fun onPause() = mapView.onPause()
    fun onStop() = mapView.onStop()
    fun onLowMemory() = mapView.onLowMemory()
    fun onDestroy() = mapView.onDestroy()

    companion object {
        private const val ROUTES_SOURCE = "roadguard-routes"
        private const val ROUTES_LAYER = "roadguard-route-lines"
    }
}
