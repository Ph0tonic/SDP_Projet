package ch.epfl.sdp.ui.maps

import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng

object MapUtils {

    const val DEFAULT_LATITUDE: Double = 47.39778846550371
    const val DEFAULT_LONGITUDE: Double = 8.545970150745575
    const val DEFAULT_ZOOM: Double = 9.0
    const val ZOOM_TOLERANCE: Double = 2.0

    private fun loadLastMapPositionFromPrefs(): LatLng {
        val context = MainApplication.applicationContext()
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
    fun saveCameraPositionAndZoomToPrefs(cameraPosition: CameraPosition) {
        val context = MainApplication.applicationContext()
        PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext()).edit()
                .putString(context.getString(R.string.prefs_latitude),
                        cameraPosition.target?.latitude.toString())
                .putString(context.getString(R.string.prefs_longitude),
                        cameraPosition.target?.longitude.toString())
                .putString(context.getString(R.string.prefs_zoom),
                        cameraPosition.zoom.toString())
                .apply()
    }

    private fun loadLastMapZoomFromPrefs(): Double {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(MainApplication.applicationContext().getString(R.string.prefs_zoom), null)
                ?.toDoubleOrNull()
                ?: DEFAULT_ZOOM
    }

    /**
     * Returns the last camera state stored in the preferences
     */
    fun getLastCameraState(): CameraPosition {
        return CameraPosition.Builder()
                .target(loadLastMapPositionFromPrefs())
                .zoom(loadLastMapZoomFromPrefs())
                .build()
    }

    /**
     * Loads the camera settings passed in parameters to the camera
     * @param latLng
     * @param zoom
     */
    fun getCameraWithParameters(latLng: LatLng, zoom: Double): CameraPosition {
        return CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build()
    }
}