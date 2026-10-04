package com.cosmicgrub.roadguard.map

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import java.util.Locale

/**
 * Stable app-facing map host. SDK-specific map types stay out of navigation/domain code.
 */
class MapLibreMapView(context: Context) : FrameLayout(context), MapRenderer {
    private val status = TextView(context).apply {
        text = "Map initializing"
        textSize = 16f
        setPadding(24, 24, 24, 24)
    }

    init { addView(status, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)) }

    override fun render(model: MapUiModel) {
        status.text = buildString {
            append("RoadGuard map")
            model.userLocation?.let {
                append("\nGPS: ")
                append(String.format(Locale.US, "%.5f", it.latitude))
                append(", ")
                append(String.format(Locale.US, "%.5f", it.longitude))
            }
            if (model.routes.isNotEmpty()) append("\nRoutes: ").append(model.routes.size)
        }
    }

    fun nativeView(): View = this
}
