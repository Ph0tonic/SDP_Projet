package ch.epfl.sdp.map

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.repository.HeatmapRepository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

class Heatmap (val heatmapData: MutableLiveData<HeatmapData>) {

    var features = ArrayList<Feature>()

    fun getFeatures(): FeatureCollection {
        return FeatureCollection.fromFeatures(features)
    }

    fun addPoint(latLng: LatLng, intensity: Double) {
        val feature: Feature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
        feature.addNumberProperty("intensity", intensity)
        features.add(feature)

        heatmapData.value!!.dataPoints.add(HeatmapPointData(latLng, intensity))
        // Notify eventual observers
        heatmapData.value = heatmapData.value
        //TODO clean up
        val repo = HeatmapRepository()
        repo.updateHeatmap("g2", heatmapData.value!!)
    }

    fun clear(){
        heatmapData.value!!.dataPoints.clear()
        // Notify eventual observers
        heatmapData.value = heatmapData.value
        //TODO clean up
        val repo = HeatmapRepository()
        repo.updateHeatmap("g2", heatmapData.value!!)
    }
}