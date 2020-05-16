package ch.epfl.sdp.map

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.epfl.sdp.database.data.HeatmapData
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class MeasureHeatmapManager(val mapView: MapView, val mapboxMap: MapboxMap, val style: Style, val upperLayerId: String): Observer<MutableMap<String, MutableLiveData<HeatmapData>>> {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val heatmapPainters = mutableMapOf<String, MapboxHeatmapPainter>()

    fun onDestroy() {
        heatmapPainters.values.forEach {
            it.onDestroy()
        }
    }

    override fun onChanged(heatmaps: MutableMap<String, MutableLiveData<HeatmapData>>) {
        // Observers for heatmap creation
        heatmaps.filter { !heatmapPainters.containsKey(it.key) }
                .forEach { (key, value) ->
                    heatmapPainters[key] = MapboxHeatmapPainter(style, value, upperLayerId)
                }

        // Remove observers on heatmap deletion
        val removedHeatmapIds = heatmapPainters.keys - heatmaps.keys
        removedHeatmapIds.forEach {
            heatmapPainters[it]!!.onDestroy()
            heatmapPainters.remove(it)
        }
    }
}