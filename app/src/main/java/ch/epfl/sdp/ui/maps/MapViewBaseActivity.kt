package ch.epfl.sdp.ui.maps

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.maps.MapView

open class MapViewBaseActivity : AppCompatActivity(){
    protected var mapView: MapView? = null

    // Override Activity lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        mapView!!.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}