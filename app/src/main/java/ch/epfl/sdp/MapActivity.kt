package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapUtils.setupCameraWithParameters
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.utils.ColorUtils


/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null
    private var currentPositionMarker: Circle? = null
    private val waypoints: MutableList<Circle> = ArrayList()

    private var currentPositionObserver = Observer<LatLng> { newLatLng: LatLng? -> newLatLng?.let { updateVehiclePosition(it) } }
    //private var currentMissionPlanObserver = Observer { latLngs: List<LatLng> -> updateMarkers(latLngs) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        val button: Button = findViewById(R.id.start_mission_button)
        button.setOnClickListener {
            val dme = DroneMissionExample.makeDroneMission()
            dme.startMission()
        }

        val offlineButton : Button = findViewById(R.id.stored_offline_map)
        offlineButton.setOnClickListener {
            startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()

        Drone.currentPositionLiveData.observe(this, currentPositionObserver)
        // viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()

        Drone.currentPositionLiveData.removeObserver(currentPositionObserver)
        //Mission.currentMissionPlanLiveData.removeObserver(currentMissionPlanObserver)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("latitude", mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString("longitude", mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString("zoom", mapboxMap?.cameraPosition?.zoom.toString())
                .apply();
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
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            // Add the marker image to map
//            style.addImage("marker-icon-id",
//                    BitmapFactory.decodeResource(
//                            this@MapsActivity.resources, R.drawable.mapbox_marker_icon_default))
            symbolManager = mapView?.let { SymbolManager(it, mapboxMap, style) }
            symbolManager!!.iconAllowOverlap = true
            circleManager = mapView?.let { CircleManager(it, mapboxMap, style) }
        }

        // Load latest location
        val latitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("latitude", null)?.toDoubleOrNull() ?: -52.6885
        val longitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("longitude", null)?.toDoubleOrNull() ?: -70.1395
        val zoom: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("zoom", null)?.toDoubleOrNull() ?: 9.0

        setupCameraWithParameters(mapboxMap, latitude, longitude, zoom)

//        mapboxMap.uiSettings.isRotateGesturesEnabled = false
//        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        // Allow to pinpoint
//        mapboxMap.addOnMapLongClickListener { point: LatLng? ->
//            viewModel.addWaypoint(point)
//            true
//        }
    }

    /** FOR THE MENU IF NEEDED **/
//    override fun _onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_maps, menu)
//        return true
//    }

//    override fun _onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle item selection
//        when (item.getItemId()) {
//            R.id.disarm -> drone.getAction().kill().subscribe()
//            R.id.land -> drone.getAction().land().subscribe()
//            R.id.return_home -> drone.getAction().returnToLaunch().subscribe()
//            R.id.takeoff -> drone.getAction().arm().andThen(drone.getAction().takeoff()).subscribe()
//            else -> return super.onOptionsItemSelected(item)
//        }
//        return true
//    }

    /**
     * Update [currentPositionMarker] position with a new [position].
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateVehiclePosition(newLatLng: LatLng) {
        if (mapboxMap == null || circleManager == null) {
            // Not ready
            return
        }

        // Add a vehicle marker and move the camera
        if (currentPositionMarker == null) {
            val circleOptions = CircleOptions()
            circleOptions.withLatLng(newLatLng)
            currentPositionMarker = circleManager!!.create(circleOptions)

            mapboxMap!!.moveCamera(CameraUpdateFactory.tiltTo(0.0))
            mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
        } else {
            currentPositionMarker!!.latLng = newLatLng
            circleManager!!.update(currentPositionMarker)
        }
    }

//    /**
//     * Update the [map] with the current mission plan waypoints.
//     *
//     * @param latLngs current mission waypoints
//     */
//    private fun updateMarkers(latLngs: List<LatLng>) {
//        if (circleManager != null) {
//            circleManager!!.delete(waypoints)
//            waypoints.clear()
//        }
//        for (latLng in latLngs) {
//            val circleOptions: CircleOptions = CircleOptions()
//                    .withLatLng(latLng)
//                    .withCircleColor(ColorUtils.colorToRgbaString(Color.BLUE))
//                    .withCircleStrokeColor(ColorUtils.colorToRgbaString(Color.BLACK))
//                    .withCircleStrokeWidth(1.0f)
//                    .withCircleRadius(12f)
//                    .withDraggable(false)
//            circleManager?.create(circleOptions)
//        }
//    }
}
