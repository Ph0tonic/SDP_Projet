package ch.epfl.sdp.ui.maps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import ch.epfl.sdp.OfflineManagerActivity
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.Mapbox.getInstance
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
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

        val mapOneButton: Button = findViewById(R.id.stored_offline_map)
        mapOneButton.setOnClickListener {
            startActivity(Intent(this@MapActivity, OfflineManagerActivity::class.java)
                    .putExtra("latitude", mapboxMap.cameraPosition.target.latitude)
                    .putExtra("longitude", mapboxMap.cameraPosition.target.longitude)
                    .putExtra("zoom", mapboxMap.cameraPosition.zoom)
            )
        }
    }
}
