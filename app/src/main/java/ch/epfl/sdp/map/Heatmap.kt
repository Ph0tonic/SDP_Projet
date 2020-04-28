package ch.epfl.sdp.map

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.firebase.dao.FirebaseHeatmapDao
import ch.epfl.sdp.firebase.data.HeatmapData
import ch.epfl.sdp.firebase.data.HeatmapPointData
import ch.epfl.sdp.firebase.repository.HeatmapDataRepository
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
        val repo = HeatmapDataRepository(FirebaseHeatmapDao())
        repo.updateHeatmap("g2", heatmapData.value!!)
    }

    fun clear(){
        heatmapData.value!!.dataPoints.clear()
        // Notify eventual observers
        heatmapData.value = heatmapData.value
        //TODO clean up
        val repo = HeatmapDataRepository(FirebaseHeatmapDao())
        repo.updateHeatmap("g2", heatmapData.value!!)
    }
}