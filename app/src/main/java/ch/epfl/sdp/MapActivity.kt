package ch.epfl.sdp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R.id.tv_latitude
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapUtils
import ch.epfl.sdp.ui.maps.MapUtils.setupCameraWithParameters
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.ColorUtils
import kotlinx.android.synthetic.main.activity_map.*

/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapActivity : MapViewBaseActivity(), OnMapReadyCallback {
    private var mapboxMap: MapboxMap? = null

    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null
    private var currentPositionMarker: Circle? = null

    private var featureCollection: FeatureCollection? = null
    private var features = ArrayList<Feature>()
    private var geoJsonSource: GeoJsonSource? = null

    private val darkRed = Color.parseColor("#E55E5E")
    private val lightRed = Color.parseColor("#F9886C")
    private val orange = Color.parseColor("#FBB03B")

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
        super.initMapView(savedInstanceState, R.layout.activity_map, R.id.mapView)
        mapView.getMapAsync(this)

        val button: Button = findViewById(R.id.start_mission_button)
        button.setOnClickListener {
            val dme = DroneMission.makeDroneMission(Drone.overflightStrategy.createFlightPath(waypoints))
            dme.startMission()
        }

        val offlineButton: Button = findViewById(R.id.stored_offline_map)
        offlineButton.setOnClickListener {
            startActivity(Intent(applicationContext, OfflineManagerActivity::class.java))
        }
        mapView.contentDescription = MAP_NOT_READY_DESCRIPTION
    }

    override fun onResume() {
        super.onResume()
        Drone.currentPositionLiveData.observe(this, currentPositionObserver)
    }

    override fun onPause() {
        super.onPause()
        Drone.currentPositionLiveData.removeObserver(currentPositionObserver)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("latitude", mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString("longitude", mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString("zoom", mapboxMap?.cameraPosition?.zoom.toString())
                .apply()
        super.onStop()
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            //symbolManager = SymbolManager(mapView!!, mapboxMap, style)
            //circleManager = CircleManager(mapView!!, mapboxMap, style)
            symbolManager = mapView.let { SymbolManager(it, mapboxMap, style) }
            symbolManager!!.iconAllowOverlap = true
            circleManager = mapView.let { CircleManager(it, mapboxMap, style) }
            lineManager = LineManager(mapView, mapboxMap, style)
            fillManager = FillManager(mapView, mapboxMap, style)

            mapboxMap.addOnMapClickListener { position ->
                onMapClicked(position)
                true
            }

            createLayersForHeatMap(style)
            /**THIS IS JUST TO ADD SOME POINTS, IT WILL BE REMOVED AFTER**/
            addPointToHeatMap(8.543434, 47.398979)
            addPointToHeatMap(8.543934, 47.398279)
            addPointToHeatMap(8.544867, 47.397426)
            addPointToHeatMap(8.543067, 47.397026)

        }

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
        mapView.contentDescription = MAP_READY_DESCRIPTION
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

        //display coordinates in the bar
        val latView: TextView = findViewById(R.id.tv_latitude)
        latView.text = prettyPrint("LAT : " + newLatLng.latitude)
        val lonView: TextView = findViewById(R.id.tv_longitude)
        lonView.text = prettyPrint("LON : " + newLatLng.longitude)

    }

    private fun prettyPrint(str : String) : CharSequence {
        return (str + "0000000000000000").subSequence(0,15)
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

    /*
    fun clearWaypoints(view: View) {

        //CAUTION : DELETES DRONE POSITION circleManager?.deleteAll()
        lineManager?.deleteAll()
        fillManager?.deleteAll()
        waypoints.clear()
    }*/


    private fun createLayersForHeatMap(style: Style) {
        createSourceData(style)
        unclusteredLayerData(style)
        clusteredLayerData(style)
    }

    fun addPointToHeatMap(longitude: Double, latitude: Double) {
        features.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)))
        featureCollection = FeatureCollection.fromFeatures(features)
        geoJsonSource!!.setGeoJson(featureCollection)

    }

    private fun createSourceData(style: Style) {
        geoJsonSource = GeoJsonSource(getString(R.string.heatmap_source_ID), GeoJsonOptions().withCluster(true))
        geoJsonSource!!.setGeoJson(featureCollection)
        style.addSource(geoJsonSource!!)
    }

    private fun unclusteredLayerData(style: Style) {
        val unclustered = CircleLayer("unclustered-points", getString(R.string.heatmap_source_ID))
        unclustered.setProperties(
                circleColor(orange),
                circleRadius(20f),
                circleBlur(1f))
        unclustered.setFilter(neq(get("cluster"), literal(true)))
        style.addLayerBelow(unclustered, getString(R.string.below_layer_id))
    }

    private fun clusteredLayerData(style: Style) {
        val layers = arrayOf(
                intArrayOf(4, darkRed),
                intArrayOf(2, lightRed),
                intArrayOf(0, orange))
        for (i in layers.indices) {
            val circles = CircleLayer("cluster-$i", getString(R.string.heatmap_source_ID))
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(60f),
                    circleBlur(1f)
            )
            val pointCount: Expression = toNumber(get("point_count"))
            circles.setFilter(
                    if (i == 0) gte(pointCount, literal(layers[i][0])) else all(
                            gte(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            )
            style.addLayerBelow(circles, getString(R.string.below_layer_id))
        }
    }
}


