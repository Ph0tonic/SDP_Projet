package ch.epfl.sdp.ui.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.Mapbox.getInstance
import com.mapbox.mapboxsdk.maps.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this);
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
        MapUtils.saveCameraPositionAndZoomToPrefs(this, mapboxMap!!)
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
        mapboxMap.setStyle(Style.MAPBOX_STREETS)

        MapUtils.setupCameraAsLastTimeUsed(this, mapboxMap)
    }
}