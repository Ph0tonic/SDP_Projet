package ch.epfl.sdp.map

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ch.epfl.sdp.database.data.HeatmapData
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class MapboxHeatmapPainter(style: Style,
                           lifecycleOwner: LifecycleOwner,
                           val heatmapData: MutableLiveData<HeatmapData>,
                           belowLayerId: String) : MapboxPainter {

    companion object {
        private val BLUE = Expression.rgb(0, 0, 255)
        private val CYAN = Expression.rgb(0, 255, 255)
        private val GREEN = Expression.rgb(0, 255, 0)
        private val ORANGE = Expression.rgb(255, 255, 0)
        private val RED = Expression.rgb(255, 0, 0)

        private fun unclusteredLayerData(heatmapId: String, style: Style, belowLayerId: String) {
            val unclustered = CircleLayer("unclustered-points$heatmapId", heatmapId)
            unclustered.setProperties(
                    PropertyFactory.circleColor(
                            Expression.interpolate(Expression.linear(), Expression.get("intensity"),
                                    Expression.stop(8, BLUE),
                                    Expression.stop(8.5, CYAN),
                                    Expression.stop(9, GREEN),
                                    Expression.stop(9.5, ORANGE),
                                    Expression.stop(10.0, RED)
                            )
                    ),
                    PropertyFactory.circleRadius(40f),
                    PropertyFactory.circleBlur(1.5f))
            unclustered.setFilter(Expression.neq(Expression.get("cluster"), Expression.literal(true)))
            style.addLayerBelow(unclustered, belowLayerId)
        }

        private fun clusteredLayerData(heatmapId: String, style: Style, belowLayerId: String) {
            val clustered = CircleLayer("clustered-points-$heatmapId", heatmapId)
            clustered.setProperties(
                    PropertyFactory.circleColor(RED),
                    PropertyFactory.circleRadius(40f),
                    PropertyFactory.circleBlur(1f)
            )
            clustered.setFilter(Expression.eq(Expression.get("cluster"), Expression.literal(true)))
            style.addLayerBelow(clustered, belowLayerId)
        }
    }

    private val geoJsonSource: GeoJsonSource = GeoJsonSource(heatmapData.value!!.uuid, GeoJsonOptions()
            .withCluster(true)
            .withClusterProperty("intensities", Expression.literal("+"), Expression.get("intensity"))
            .withClusterMaxZoom(13)
    )
    private val heatmapRedrawObserver = Observer<HeatmapData> { paint() }

    init {
        val heatmapId = heatmapData.value!!.uuid!!
        style.addSource(geoJsonSource)
        heatmapData.observe(lifecycleOwner, heatmapRedrawObserver)
        unclusteredLayerData(heatmapId, style, belowLayerId)
        clusteredLayerData(heatmapId, style, belowLayerId)

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

    override fun onDestroy() {
        //Nothing yet
    }
}