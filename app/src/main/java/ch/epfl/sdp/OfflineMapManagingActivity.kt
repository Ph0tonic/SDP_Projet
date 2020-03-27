package ch.epfl.sdp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import org.json.JSONObject

class OfflineMapManagingActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_offline_map)

        mapView = findViewById(R.id.store_mapoffline_mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

    }

    fun downloadOfflineMap(view : View){
        val longitude = mapboxMap?.cameraPosition?.target!!.longitude
        val latitude = mapboxMap?.cameraPosition?.target!!.latitude

        // Set up the OfflineManager
        val offlineManager = OfflineManager.getInstance(this)

        // Create a bounding box for the offline region
        val latLngBounds = LatLngBounds.Builder()
                .include(LatLng(latitude + 0.1, longitude + 0.1)) // Northeast
                .include(LatLng(latitude - 0.1, longitude - 0.1)) // Southwest
                .build()

        // Define the offline region
        val definition = OfflineTilePyramidRegionDefinition(
                Style.SATELLITE,
                latLngBounds,
                10.0,
                20.0,this.resources.displayMetrics.density)

        // Implementation that uses JSON to store a map as the offline region name.
        var metadata: ByteArray?
        try {
            val jsonObject = JSONObject()
            val JSON_FIELD_REGION_NAME = "test"
            jsonObject.put(JSON_FIELD_REGION_NAME, "Yosemite National Park")
            val json = jsonObject.toString()
            val JSON_CHARSET = "test_name"
            metadata = json.toByteArray(charset(JSON_CHARSET))
        } catch (exception: Exception) {
            Log.e("---------------> TAG", "Failed to encode metadata: " + exception.message)
            metadata = null
        }


    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS)

        val mapNumber = intent.getIntExtra("ButtonId", 0)
        var latitude : Double = -52.6885
        var longitude : Double = -70.1395
        var zoom : Double = 9.0

        when(mapNumber) {
            0 -> {
                // Load latest location
                val latitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("latitude", null)?.toDouble() ?: -52.6885
                val longitude: Double = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("longitude", null)?.toDouble() ?: -70.1395
                val zoom: Double = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("zoom", null)?.toDouble() ?: 9.0
            }
            1 -> {
                latitude = 45.980537
                longitude = 7.641618
            }
            2 -> {
                latitude = 45.832622
                longitude = 6.865175
            }
            3 -> {
                latitude = 48.858093
                longitude = 2.294694
            }
        }

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