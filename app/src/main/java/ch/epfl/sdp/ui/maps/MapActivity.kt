package ch.epfl.sdp.ui.maps

import android.R
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import io.mavsdk.System;
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.annotation.*
import io.mavsdk.mavsdkserver.MavsdkServer

import io.reactivex.disposables.Disposable


/**
 * Main Activity to display map and create missions.
 * 1. Take off
 * 2. Long click on map to add a waypoint
 * 3. Hit play to start mission.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var circleManager: CircleManager? = null
    private var symbolManager: SymbolManager? = null
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    //private var viewModel: MapsViewModel? = null
    private var currentPositionMarker: Symbol? = null
    private var drone: System? = null
    private val waypoints: MutableList<Circle> = ArrayList()
    private val disposables: MutableList<Disposable> = ArrayList()
    private var currentPositionObserver: Observer<LatLng>
    private var currentMissionPlanObserver: Observer<List<LatLng>>

    init {
        currentPositionObserver = Observer<LatLng> { newLatLng: LatLng? -> newLatLng?.let { updateVehiclePosition(it) } }
        currentMissionPlanObserver = Observer<List<LatLng>> { latLngs: List<LatLng> -> updateMarkers(latLngs) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_maps)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener { v: View? -> viewModel.startMission(drone) }
    }

    public override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        viewModel.currentPositionLiveData.observe(this, currentPositionObserver)
        viewModel.currentMissionPlanLiveData.observe(this, currentMissionPlanObserver)
        Thread(Runnable {
            val mavsdkServer = MavsdkServer()
            mavsdkServer.run("udp://:14540", 50020)
        }).start()
        drone = System(BACKEND_IP_ADDRESS, 50020)
        disposables.add(drone!!.telemetry.flightMode.distinct()
                .subscribe { flightMode -> Log.d(this.javaClass.simpleName,"flight mode: $flightMode") })
        disposables.add(drone!!.telemetry.armed.distinct()
                .subscribe { armed -> Log.d(this.javaClass.simpleName,"armed: $armed") })
        disposables.add(drone!!.telemetry.position.subscribe { position ->
            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
            viewModel.currentPositionLiveData.postValue(latLng)
        })
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
        viewModel.currentPositionLiveData.removeObserver(currentPositionObserver)
        viewModel.currentMissionPlanLiveData.removeObserver(currentMissionPlanObserver)
        for (disposable in disposables) {
            disposable.dispose()
        }
        drone?.dispose()
        drone = null
    }

    public override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.disarm -> drone.getAction().kill().subscribe()
            R.id.land -> drone.getAction().land().subscribe()
            R.id.return_home -> drone.getAction().returnToLaunch().subscribe()
            R.id.takeoff -> drone.getAction().arm().andThen(drone.getAction().takeoff()).subscribe()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Update [currentPositionMarker] position with a new [position].
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateVehiclePosition(newLatLng: LatLng) {
        if (newLatLng == null || map == null || symbolManager == null) {
            // Not ready
            return
        }

        // Add a vehicle marker and move the camera
        if (currentPositionMarker == null) {
            val symbolOptions = SymbolOptions()
            symbolOptions.withLatLng(newLatLng)
            symbolOptions.withIconImage("marker-icon-id")
            currentPositionMarker = symbolManager.create(symbolOptions)
            map!!.moveCamera(CameraUpdateFactory.tiltTo(0.0))
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 14.0))
        } else {
            currentPositionMarker.setLatLng(newLatLng)
            symbolManager.update(currentPositionMarker)
        }
    }

    /**
     * Update the [map] with the current mission plan waypoints.
     *
     * @param latLngs current mission waypoints
     */
    private fun updateMarkers(latLngs: List<LatLng>) {
        if (circleManager != null) {
            circleManager.delete(waypoints)
            waypoints.clear()
        }
        for (latLng in latLngs) {
            val circleOptions: CircleOptions = CircleOptions()
                    .withLatLng(latLng)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.BLUE))
                    .withCircleStrokeColor(ColorUtils.colorToRgbaString(Color.BLACK))
                    .withCircleStrokeWidth(1.0f)
                    .withCircleRadius(12f)
                    .withDraggable(false)
            circleManager.create(circleOptions)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.uiSettings.isRotateGesturesEnabled = false
        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        mapboxMap.addOnMapLongClickListener { point: LatLng? ->
            viewModel.addWaypoint(point)
            true
        }
        mapboxMap.setStyle(Style.LIGHT) { style ->
            // Add the marker image to map
            style.addImage("marker-icon-id",
                    BitmapFactory.decodeResource(
                            this@MapsActivity.resources, R.drawable.mapbox_marker_icon_default))
            symbolManager = SymbolManager(mapView, map, style)
            symbolManager.setIconAllowOverlap(true)
            circleManager = CircleManager(mapView, map, style)
        }
        map = mapboxMap
    }

    companion object {
        const val BACKEND_IP_ADDRESS = "127.0.0.1"
    }

}