package ch.epfl.sdp.ui.missionDesign

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.R
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapUtils
import com.mapbox.mapboxsdk.Mapbox.getInstance
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils


class TrajectoryPlanningActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private var symbolManager: SymbolManager? = null
    private var circleManager: CircleManager? = null
    private var lineManager: LineManager? = null
    private var fillManager: FillManager? = null

    var waypoints = arrayListOf<LatLng>()

    companion object {
        private const val MAP_NOT_READY_DESCRIPTION: String = "MAP NOT READY"
        private const val MAP_READY_DESCRIPTION: String = "MAP READY"

        private const val PATH_THICKNESS: Float = 5F
        private const val REGION_FILL_OPACITY: Float = 0.5F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.trajectory_planning_map)

        mapView = findViewById(R.id.trajectory_planning_mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Used to detect when the map is ready in tests
        mapView.contentDescription = MAP_NOT_READY_DESCRIPTION
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap)
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        MapUtils.setupCameraAsLastTimeUsed(this, mapboxMap)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style: Style? ->

            fillManager = FillManager(mapView, mapboxMap, style!!)
            symbolManager = SymbolManager(mapView, mapboxMap, style)
            lineManager = LineManager(mapView, mapboxMap, style)
            circleManager = CircleManager(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }
        }

        // Used to detect when the map is ready in tests
        mapView.contentDescription = MAP_READY_DESCRIPTION
    }

    fun onMapClicked(position: LatLng): Boolean {
        if (waypoints.size < 4) {
            waypoints.add(position)
            drawPinpoint(position)

            if (waypoints.isNotEmpty()) {
                drawRegion(waypoints)
            }

            if (waypoints.size == 4) {
                drawPath(Drone.overflightStrategy.createFlightPath(waypoints))
            }
        }
        return true
    }

    private fun drawPath(path: List<LatLng>) {
        lineManager?.create(LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS))
    }

    private fun drawRegion(corners: List<LatLng>) {
        // Draw the fill
        val fillOption = FillOptions()
                .withLatLngs(listOf(waypoints))
                .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                .withFillOpacity(REGION_FILL_OPACITY)
        fillManager?.deleteAll()
        fillManager?.create(fillOption)

        //Draw the borders

        // Make it loop
        val linePoints = arrayListOf<LatLng>().apply {
            addAll(corners)
            add(corners[0])
        }
        val lineOptions = LineOptions()
                .withLatLngs(linePoints)
                .withLineColor(ColorUtils.colorToRgbaString(Color.LTGRAY))
        lineManager?.deleteAll()
        lineManager?.create(lineOptions)
    }

    private fun drawPinpoint(pinpoints: LatLng) {
        val circleOptions = CircleOptions()
                .withLatLng(pinpoints)
                .withDraggable(true)
        circleManager?.create(circleOptions)
    }

    fun returnPathToMissionDesign(view: View) {
        val resultIntent = Intent()
        resultIntent.putExtra("waypoints", waypoints)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun clearWaypoints(view: View) {
        circleManager?.deleteAll()
        lineManager?.deleteAll()
        fillManager?.deleteAll()
        waypoints.clear()
    }
}
