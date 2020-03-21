package ch.epfl.sdp.ui.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.Mapbox.getInstance
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment


class SupportMapFragmentActivity : AppCompatActivity() {

    lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_map_fragment)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        getInstance(this, getString(R.string.mapbox_access_token))

        // Create supportMapFragment
        if (savedInstanceState == null) {
            // Create fragment
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

            // Build mapboxMap
            val options = MapboxMapOptions.createFromAttributes(this, null)

            //TODO: Load latest location
            val latitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("latitude", null)?.toDouble() ?: -52.6885
            val longitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("longitude", null)?.toDouble() ?: -70.1395
            val zoom: Double = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("zoom", null)?.toDouble() ?: 9.0

            options.camera(CameraPosition.Builder()
                    .target(LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build())

            // Create map fragment
            mapFragment = SupportMapFragment.newInstance(options)

            // Add map fragment to parent container
            transaction.add(R.id.container, mapFragment, "com.mapbox.map")
            transaction.commit()
        } else {
            mapFragment = supportFragmentManager.findFragmentByTag("com.mapbox.map") as SupportMapFragment
        }
        mapFragment.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mapFragment.getMapAsync { mapboxMap ->
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("latitude", mapboxMap.cameraPosition.target.latitude.toString())
                    .putString("longitude", mapboxMap.cameraPosition.target.longitude.toString())
                    .putString("zoom", mapboxMap.cameraPosition.zoom.toString())
                    .apply();
        }
    }
}
