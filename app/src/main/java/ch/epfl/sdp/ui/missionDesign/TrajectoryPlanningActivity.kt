package ch.epfl.sdp.ui.missionDesign

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.R
import ch.epfl.sdp.ui.maps.MapUtils
import com.mapbox.mapboxsdk.Mapbox.getInstance
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.utils.ColorUtils
import com.mapbox.mapboxsdk.utils.ColorUtils.colorToRgbaString


class TrajectoryPlanningActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var  symbolManager: SymbolManager? = null
    private var  circleManager: CircleManager? = null
    private var  lineManager: LineManager? = null
    private var fillManager: FillManager? = null

    var waypoints = arrayListOf<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.trajectory_planning_map)

        mapView = findViewById(R.id.trajectory_planning_mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        // Used to detect when the map is ready in tests
        mapView?.contentDescription = "MAP NOT READY"
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap)
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        MapUtils.setupCameraAsLastTimeUsed(this, mapboxMap)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style: Style? ->

            symbolManager = SymbolManager(mapView!!, mapboxMap, style!!)
            circleManager = CircleManager(mapView!!, mapboxMap, style)
            lineManager = LineManager(mapView!!, mapboxMap, style)
            fillManager = FillManager(mapView!!, mapboxMap,style)

            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }
        }

        // Used to detect when the map is ready in tests
        mapView?.contentDescription = "MAP READY"
    }

    fun onMapClicked(position: LatLng): Boolean{
        if (waypoints.size < 4){
            waypoints.add(position)
            val circleOptions = CircleOptions()
                    .withLatLng(position)
                    .withDraggable(true)
            circleManager?.create(circleOptions)

            if (waypoints.isNotEmpty()){
                val linePoints = arrayListOf<LatLng>().apply {
                    addAll(waypoints)
                    add(waypoints[0])
                }

                val fillOption = FillOptions()
                        .withLatLngs(listOf(waypoints))
                        .withFillColor(ColorUtils.colorToRgbaString(Color.WHITE))
                        .withFillOpacity(0.5F)
                fillManager?.create(fillOption)

                val lineOptions = LineOptions().withLatLngs(linePoints)
                lineManager?.deleteAll()
                lineManager?.create(lineOptions)

            }
        }
        return true
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
