package ch.epfl.sdp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style

class OfflineMapManagingActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
                setContentView(R.layout.fragment_offline_map_managing)
                mapView = findViewById(R.id.mapView)
                mapView?.onCreate(savedInstanceState)
                mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {

                // Map is set up and the style has loaded. Now you can add data or make other map adjustments

            }

        }
    }

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_offline_map)

        mapView = findViewById(R.id.store_mapoffline_mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this);
    }

    /*
    override fun onMapReady(mapboxMap: MapboxMap) {
        // Set up the OfflineManager
        val offlineManager = OfflineManager.getInstance(Mapbox.getApplicationContext())

        // Create a bounding box for the offline region
        val latLngBounds = LatLngBounds.Builder()
                .include(LatLng(37.7897, -119.5073)) // Northeast
                .include(LatLng(37.6744, -119.6815)) // Southwest
                .build()

        // Define the offline region
        val definition = OfflineTilePyramidRegionDefinition(
                mapboxMap.style.toString(),
                latLngBounds,
                10.0,
                20.0,Mapbox.getApplicationContext().getResources().getDisplayMetrics().density)
    }
    */
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS)

        // Load latest location
        val latitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("latitude", null)?.toDouble() ?: -52.6885
        val longitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("longitude", null)?.toDouble() ?: -70.1395
        val zoom: Double = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("zoom", null)?.toDouble() ?: 9.0

        mapboxMap.cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(zoom)
                .build()
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

}