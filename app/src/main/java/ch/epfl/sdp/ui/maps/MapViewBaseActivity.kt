package ch.epfl.sdp.ui.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView

open class MapViewBaseActivity : AppCompatActivity() {
    lateinit var mapView: MapView

    protected fun initMapView(savedInstanceState: Bundle?, contentViewId: Int, mapViewId: Int) {
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(contentViewId)
        // Set up the MapView
        mapView = findViewById(mapViewId)
        mapView.onCreate(savedInstanceState)
    }

    // Override Activity lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}