package ch.epfl.sdp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapUtils
import ch.epfl.sdp.ui.maps.MapUtils.setupCameraWithParameters
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import ch.epfl.sdp.ui.missionDesign.TrajectoryPlanningActivity
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
class NewMapActivity : MapViewBaseActivity(), OnMapReadyCallback {
    private var mapboxMap: MapboxMap? = null

    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null
    private var currentPositionMarker: Circle? = null

    private var currentPositionObserver = Observer<LatLng> { newLatLng: LatLng? -> newLatLng?.let { updateVehiclePosition(it) } }

    //Trajectory Planning
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
        //R.layout.trajectory_planning_map
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        val button: Button = findViewById(R.id.start_mission_button)
        button.setOnClickListener {
            val dme = DroneMissionExample.makeDroneMission()
            dme.startMission()
        }

        val offlineButton: Button = findViewById(R.id.stored_offline_map)
        offlineButton.setOnClickListener {
            startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
        }
        mapView?.contentDescription = MAP_NOT_READY_DESCRIPTION
    }

    override fun onResume() {
        super.onResume()

        Drone.currentPositionLiveData.observe(this, currentPositionObserver)
        // viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
    }

    override fun onPause() {
        super.onPause()

        Drone.currentPositionLiveData.removeObserver(currentPositionObserver)
        //Mission.currentMissionPlanLiveData.removeObserver(currentMissionPlanObserver)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("latitude", mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString("longitude", mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString("zoom", mapboxMap?.cameraPosition?.zoom.toString())
                .apply()
        super.onStop()
        //TODO - CHECK : the following line was between super.onStop() and mapView?.onStop() in Trajectory Planning
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap)

    }

    //TODO
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            // Add the marker image to map
//            style.addImage("marker-icon-id",
//                    BitmapFactory.decodeResource(
//                            this@MapsActivity.resources, R.drawable.mapbox_marker_icon_default))
            symbolManager = mapView.let { SymbolManager(it, mapboxMap, style) }
            symbolManager!!.iconAllowOverlap = true
            circleManager = mapView.let { CircleManager(it, mapboxMap, style) }
        }

        /**
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style: Style? ->

            fillManager = FillManager(mapView!!, mapboxMap,style!!)
            symbolManager = SymbolManager(mapView!!, mapboxMap, style)
            lineManager = LineManager(mapView!!, mapboxMap, style)
            circleManager = CircleManager(mapView!!, mapboxMap, style)

            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }
        }*/

        // Load latest location
        /** TrajectoryPlanningActivity was : MapUtils.setupCameraAsLastTimeUsed(this, mapboxMap)*/
        val latitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("latitude", null)?.toDoubleOrNull() ?: -52.6885
        val longitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("longitude", null)?.toDoubleOrNull() ?: -70.1395
        val zoom: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("zoom", null)?.toDoubleOrNull() ?: 9.0

        setupCameraWithParameters(mapboxMap, LatLng(latitude, longitude), zoom)

        // Used to detect when the map is ready in tests
        mapView?.contentDescription = MAP_READY_DESCRIPTION
    }


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

    /** Trajectory Planning **/

    fun onMapClicked(position: LatLng): Boolean{
        if (waypoints.size < 4){
            waypoints.add(position)
            drawPinpoint(position)

            if (waypoints.isNotEmpty()){
                drawRegion(waypoints)
            }

            if (waypoints.size == 4){
                drawPath(Drone.overflightStrategy.createFlightPath(waypoints))
            }
        }
        return true
    }

    private fun drawPath(path: List<LatLng>){
        lineManager?.create(LineOptions()
                .withLatLngs(path)
                .withLineWidth(PATH_THICKNESS))
    }

    private fun drawRegion(corners: List<LatLng>){
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

    private fun drawPinpoint(pinpoints: LatLng){
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

