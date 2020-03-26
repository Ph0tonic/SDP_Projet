package ch.epfl.sdp.ui.maps

import android.content.Context
import androidx.preference.PreferenceManager
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

object MapUtils {

    private const val DEFAULT_LATITUDE: Double =  47.39778846550371
    private const val DEFAULT_LONGITUDE:Double  = 8.545970150745575
    private const val DEFAULT_ZOOM: Double = 9.0

    private fun loadLastMapPositionFromPrefs(context: Context): LatLng{
        val latitude: Double = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("latitude", null)?.toDouble() ?: DEFAULT_LATITUDE
        val longitude: Double = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("longitude", null)?.toDouble() ?: DEFAULT_LONGITUDE
        return LatLng(latitude,longitude)
    }

    /**
     * Saves the camera position and zoom to the shared preferences
     */
    fun saveCameraPositionAndZoomToPrefs(context: Context, mapboxMap: MapboxMap?){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("latitude", mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString("longitude", mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString("zoom", mapboxMap?.cameraPosition?.zoom.toString())
                .apply();
    }

    private fun loadLastMapZoomFromPrefs(context: Context): Double{
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("zoom", null)?.toDouble() ?: DEFAULT_ZOOM
    }

    /**
     * Loads and applies the camera settings stored in the preferences to the camera of the given
     * map
     */
    fun setupCameraAsLastTimeUsed(context: Context, mapboxMap: MapboxMap){
        mapboxMap.cameraPosition = CameraPosition.Builder()
                .target(loadLastMapPositionFromPrefs(context))
                .zoom(loadLastMapZoomFromPrefs(context))
                .build()
    }
}