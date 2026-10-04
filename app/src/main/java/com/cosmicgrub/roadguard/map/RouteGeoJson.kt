package com.cosmicgrub.roadguard.map

import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

object RouteGeoJson {
    fun collection(lines: List<RouteLine>): FeatureCollection =
        FeatureCollection.fromFeatures(lines.map { line ->
            Feature.fromGeometry(LineString.fromLngLats(line.points.map {
                Point.fromLngLat(it.longitude, it.latitude)
            })).also { feature ->
                feature.addStringProperty("routeId", line.id)
                feature.addBooleanProperty("selected", line.selected)
            }
        })
}
