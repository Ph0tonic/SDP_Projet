package ch.epfl.sdp.map

import ch.epfl.sdp.firebase.data.HeatmapData
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

class Heatmap {

    private var heatmapData = HeatmapData()
    var features = ArrayList<Feature>()

    fun getFeatures(): FeatureCollection {
        val test = FeatureCollection.fromFeatures(features)
//        test.features()?.add(features.first())
        return FeatureCollection.fromFeatures(features)
    }

    fun addPoint(latLng: LatLng, intensity: Double) {
        val feature: Feature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
        feature.addNumberProperty("intensity", intensity)
        features.add(feature)
    }
}