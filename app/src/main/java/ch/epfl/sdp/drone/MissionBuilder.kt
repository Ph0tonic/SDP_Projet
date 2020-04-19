package ch.epfl.sdp.drone

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import ch.epfl.sdp.searcharea.SearchArea
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.utils.ColorUtils

class MissionBuilder(lifecycleOwner: LifecycleOwner, private val startingLocation: LiveData<LatLng>, private val searchArea: LiveData<SearchArea>, private val strategy: LiveData<OverflightStrategy>) {
    companion object {
        private const val PATH_THICKNESS: Float = 2F
    }

    enum class MissionBuilderStatus {
        UNINITIALIZED, INCOMPATIBLE_STRATEGY_AND_SEARCHAREA, OK
    }

    var status = MissionBuilderStatus.UNINITIALIZED
    private var mounted: Boolean = false

    private lateinit var cachedSearchArea: SearchArea

    private var generalObserver: Observer<Any>

    private lateinit var lineManager: LineManager
    private lateinit var lineArea: Line

    private lateinit var path: List<LatLng>

    init {
        generalObserver = Observer {
            computeMissionPath()
        }
        val strategyObserver = Observer<OverflightStrategy> {
            if (!it.acceptArea(searchArea.value!!)) {
                status = MissionBuilderStatus.INCOMPATIBLE_STRATEGY_AND_SEARCHAREA
            } else {
                computeMissionPath()
            }
        }
        val searchAreaObserver = Observer<SearchArea> {
            if (::cachedSearchArea.isInitialized) {
                //Remove old observers
                cachedSearchArea.getAdditionalProps().removeObserver(this.generalObserver)
                cachedSearchArea.getLatLng().removeObserver(this.generalObserver)
            }
            this.cachedSearchArea = it
            cachedSearchArea.getAdditionalProps().observe(lifecycleOwner, this.generalObserver)
            cachedSearchArea.getLatLng().observe(lifecycleOwner, this.generalObserver)

            computeMissionPath()
        }

        startingLocation.observe(lifecycleOwner, generalObserver)
        strategy.observe(lifecycleOwner, strategyObserver)
        searchArea.observe(lifecycleOwner, searchAreaObserver)

        if (searchArea.value != null) {
            searchArea.value!!.getLatLng().observe(lifecycleOwner, this.generalObserver)
            searchArea.value!!.getAdditionalProps().observe(lifecycleOwner, this.generalObserver)
        }
    }

    fun mount(mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        require(!mounted) { "A mission builder cannot be mounted twice" }
        lineManager = LineManager(mapView, mapboxMap, style)
        mounted = true

        computeMissionPath()
    }

    fun getMission(): List<LatLng> {
        return path
    }

    private fun computeMissionPath() {
        if (
                status == MissionBuilderStatus.OK &&
                searchArea.value != null && searchArea.value?.isComplete()!!
                && startingLocation.value != null
                && strategy.value != null
        ) {
            path = strategy.value!!.createFlightPath(startingLocation.value!!, searchArea.value!!)
            displayStrategyPath(path)
        }
    }

    private fun displayStrategyPath(path: List<LatLng>) {
        if (!mounted || path.isEmpty()) return

        if (!::lineArea.isInitialized) {
            lineManager.deleteAll()
            val lineOptions = LineOptions()
                    .withLatLngs(path)
                    .withLineWidth(PATH_THICKNESS)
                    .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
            lineArea = lineManager.create(lineOptions)
        } else {
            lineArea.latLngs = path
            lineManager.update(lineArea)
        }
    }
}
