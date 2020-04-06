package ch.epfl.sdp.ui.maps

import android.content.Context
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

object MapUtils {

    const val DEFAULT_LATITUDE: Double = 47.39778846550371
    const val DEFAULT_LONGITUDE: Double = 8.545970150745575
    const val DEFAULT_ZOOM: Double = 9.0

    private fun loadLastMapPositionFromPrefs(context: Context): LatLng {
        val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val latitude: Double = defaultSharedPrefs
                .getString(context.getString(R.string.prefs_latitude), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_LATITUDE
        val longitude: Double = defaultSharedPrefs
                .getString(context.getString(R.string.prefs_longitude), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_LONGITUDE
        return LatLng(latitude, longitude)
    }

    /**
     * Saves the camera position and zoom to the shared preferences
     */
    fun saveCameraPositionAndZoomToPrefs(context: Context, mapboxMap: MapboxMap?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.prefs_latitude),
                        mapboxMap?.cameraPosition?.target?.latitude.toString())
                .putString(context.getString(R.string.prefs_longitude),
                        mapboxMap?.cameraPosition?.target?.longitude.toString())
                .putString(context.getString(R.string.prefs_zoom),
                        mapboxMap?.cameraPosition?.zoom.toString())
                .apply()
    }

    private fun loadLastMapZoomFromPrefs(context: Context): Double {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_zoom), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_ZOOM
    }

    /**
     * Loads and applies the camera settings stored in the preferences to the camera of the given
     * map
     */
    fun setupCameraAsLastTimeUsed(context: Context, mapboxMap: MapboxMap) {
        setupCameraWithParameters(mapboxMap,
                loadLastMapPositionFromPrefs(context),
                loadLastMapZoomFromPrefs(context))
    }

    /**
     * Loads and applies the camera settings passed in parameters to the camera of the given map
     * @param mapboxMap
     * @param latLng
     * @param zoom
     */
    fun setupCameraWithParameters(mapboxMap: MapboxMap?, latLng: LatLng, zoom: Double) {
        mapboxMap?.cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build()
    }
}