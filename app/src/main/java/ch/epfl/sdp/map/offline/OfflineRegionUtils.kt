package ch.epfl.sdp.map.offline

import android.graphics.Camera
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.DownloadProgressBarUtils.hideProgressBar
import ch.epfl.sdp.ui.maps.offline.OfflineManagerActivity
import com.google.type.LatLng
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.json.JSONObject
import timber.log.Timber
import java.nio.charset.Charset


object OfflineRegionUtils {
    fun deleteOfflineRegion(offRegion: OfflineRegion, progressBar: ProgressBar, mapView: MapView) {
        mapView.contentDescription = MainApplication.applicationContext().getString(R.string.map_deleting)
        offRegion.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() { // Once the region is deleted, remove the
                // progressBar and display a toast
                hideProgressBar(progressBar)
                val context = MainApplication.applicationContext()
                Toast.makeText(context, context.getString(R.string.toast_region_deleted), Toast.LENGTH_LONG).show()
                mapView.contentDescription = MainApplication.applicationContext().getString(R.string.map_ready)
            }

            override fun onError(error: String) {
                hideProgressBar(progressBar)
                throw Exception("Error : $error")
            }
        })
    }

    // Get the region name from the offline region metadata
    fun getRegionName(offlineRegion: OfflineRegion): String {
        return JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                .getString(OfflineManagerActivity.JSON_FIELD_REGION_NAME)
    }

    fun getRegionLocation(offlineRegion: OfflineRegion): CameraPosition {
        val lat = JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                .getString(OfflineManagerActivity.JSON_FIELD_REGION_LOCATION_LATITUDE).toDouble()
        val lng = JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                .getString(OfflineManagerActivity.JSON_FIELD_REGION_LOCATION_LONGITUDE).toDouble()
        val zoom = JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                .getString(OfflineManagerActivity.JSON_FIELD_REGION_ZOOM).toDouble()
        return CameraPosition.Builder().target(com.mapbox.mapboxsdk.geometry.LatLng(lat, lng)).zoom(zoom).build()
    }

    fun getRegionById(id: Long, callback: (OfflineRegion?) -> Unit) {
        OfflineManager.getInstance(MainApplication.applicationContext()).listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<out OfflineRegion>?) {
                callback(offlineRegions?.filter { it.id == id }?.first())
            }

            override fun onError(error: String?) {
                callback(null)
            }
        })
    }

    fun showErrorAndToast(message: String) {
        Timber.e(message)
        Toast.makeText(MainApplication.applicationContext(), message, Toast.LENGTH_LONG).show()
    }
}