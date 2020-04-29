package ch.epfl.sdp.map

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.epfl.sdp.database.data.HeatmapData
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class MapboxHeatmapPainter(style: Style,
                           lifecycleOwner: LifecycleOwner,
                           val heatmapData: MutableLiveData<HeatmapData>) {

    private val geoJsonSource: GeoJsonSource = GeoJsonSource(heatmapData.value!!.uuid, GeoJsonOptions()
            .withCluster(true)
            .withClusterProperty("intensities", Expression.literal("+"), Expression.get("intensity"))
            .withClusterMaxZoom(13)
    )
    private val heatmapRedrawObserver = Observer<HeatmapData> { paint() }

    init {
        style.addSource(geoJsonSource)
        heatmapData.observe(lifecycleOwner, heatmapRedrawObserver)

        // first call is not triggered by observer
        paint()
    }

    fun destroy(style: Style) {
        style.removeSource(geoJsonSource)
        heatmapData.removeObserver(heatmapRedrawObserver)
    }

    private fun paint() {
        val features = FeatureCollection.fromFeatures(
                heatmapData.value!!.dataPoints.map {
                    val feature = Feature.fromGeometry(Point.fromLngLat(it.position!!.longitude, it.position!!.latitude))
                    feature.addNumberProperty("intensity", it.intensity)
                    feature
                }.toMutableList()
        )
        geoJsonSource.setGeoJson(features)
    }

    fun addPoint(latLng: LatLng, intensity: Double) {
        TODO("Implement me")
//        val feature: Feature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
//        feature.addNumberProperty("intensity", intensity)
//        features.add(feature)
//
//        heatmapData.value!!.dataPoints.add(HeatmapPointData(latLng, intensity))
//        // Notify eventual observers
//        heatmapData.value = heatmapData.value
//        //TODO clean up
//        val repo = HeatmapRepository()
//        repo.updateHeatmap("g2", heatmapData.value!!)
    }

//    fun clear() {
//        heatmapData.value!!.dataPoints.clear()
//        // Notify eventual observers
//        heatmapData.value = heatmapData.value
//        //TODO clean up
//        val repo = HeatmapRepository()
//        repo.updateHeatmap("g2", heatmapData.value!!)
//    }
}