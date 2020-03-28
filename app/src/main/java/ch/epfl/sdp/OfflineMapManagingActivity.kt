package ch.epfl.sdp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import com.mapbox.mapboxsdk.offline.OfflineManager.CreateOfflineRegionCallback
import com.mapbox.mapboxsdk.offline.OfflineManager.ListOfflineRegionsCallback
import com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionDeleteCallback
import com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Download and view an offline map using the Mapbox Android SDK.
 */
class OfflineMapManagingActivity : AppCompatActivity() {
    private var isEndNotified = false
    private var progressBar: ProgressBar? = null
    private var mapView: MapView? = null
    private var offlineManager: OfflineManager? = null
    private var statusquoi : Long? = null
    private var completeStatus : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this,
                getString(ch.epfl.sdp.R.string.mapbox_access_token))

        val latitude = intent.getDoubleExtra("latitude", -52.6885)
        val longitude : Double = intent.getDoubleExtra("longitude", -70.1395)

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_offline_map)

        mapView = findViewById(R.id.store_mapoffline_mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(OnMapReadyCallback { mapboxMap ->
            mapboxMap.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(latitude, longitude))
                    .build()
            mapboxMap.setStyle(Style.OUTDOORS)
            { style ->
                // Set up the OfflineManager
                offlineManager = OfflineManager.getInstance(this@OfflineMapManagingActivity)

                // Create a bounding box for the offline region
                val area = 0.001 // for the moment I define a small square
                Log.d("DEBUG", "DEBUG_DEBUGGIIIIING1 latitude NW = " + (latitude + area))
                Log.d("DEBUG", "DEBUG_DEBUGGIIIIING2 longitude NW = " + (longitude + area))

                Log.d("DEBUG", "DEBUG_DEBUGGIIIIING1 latitude SW = " + (latitude - area))
                Log.d("DEBUG", "DEBUG_DEBUGGIIIIING2 longitude SW = " + (longitude - area))

                val latLngBounds = LatLngBounds.Builder()
                        .include(LatLng(latitude + area, longitude + area)) // Northeast
                        .include(LatLng(latitude - area, latitude - area)) // Southwest
                        .build()
                // Define the offline region
                val definition = OfflineTilePyramidRegionDefinition(
                        style.url,
                        latLngBounds,
                        9.0,
                        10.0,
                        this@OfflineMapManagingActivity.resources.displayMetrics.density)
                // Set the metadata
                val metadata: ByteArray?
                metadata = try {
                    val jsonObject = JSONObject()
                    jsonObject.put(JSON_FIELD_REGION_NAME, "THE_STORED_MAP_S_NAME")
                    val json = jsonObject.toString()
                    json.toByteArray(charset(JSON_CHARSET))
                } catch (exception: Exception) {
                    Log.d("DEBUG", "DEBUG_DEBUGGIIIIING1")
                    Log.e("data encode fail: %s", exception.message)
                    null
                }
                // Create the region asynchronously
                if (metadata != null) {
                    Log.d("DEBUG", "DEBUG_DEBUGGIIIIING2")
                    offlineManager?.createOfflineRegion(
                            definition,
                            metadata,
                            object : CreateOfflineRegionCallback {
                                override fun onCreate(offlineRegion: OfflineRegion) {
                                    Log.d("DEBUG", "DEBUG_DEBUGGIIIIING3")
                                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                                    // Display the download progress bar
                                    progressBar = findViewById(R.id.progress_bar)
                                    startProgress()
                                    // Monitor the download progress using setObserver
                                    offlineRegion.setObserver(object : OfflineRegionObserver {
                                        override fun onStatusChanged(status: OfflineRegionStatus) { // Calculate the download percentage and update the progress bar
                                            statusquoi = status.requiredResourceCount
                                            completeStatus = status.completedResourceCount
                                            val percentage = if (status.requiredResourceCount >= 0) 100.0 * status.completedResourceCount / status.requiredResourceCount else 0.0
                                            if(status.isComplete){
                                                endProgress(getString(R.string.simple_offline_end_progress_success))
                                            }
                                            else if (status.isRequiredResourceCountPrecise) { // Switch to determinate state
                                                setPercentage(percentage.roundToInt())
                                            }
                                        }

                                        override fun onError(error: OfflineRegionError) { // If an error occurs, print to logcat
                                            Log.e("OFFDLE_Err", error.reason)
                                            Log.e("OFFDLE_Err", error.message)
                                        }

                                        override fun mapboxTileCountLimitExceeded(limit: Long) { // Notify if offline region exceeds maximum tile count
                                            Log.e("OFFDLE_TileCount","Too many tiles : " + limit + " Trying to download " + statusquoi + "did download :" + completeStatus)
                                        }
                                    })
                                }

                                override fun onError(error: String) {
                                    Log.e("Error: %s", error)
                                }
                            })
                }
            }
        })
    }

    public override fun onResume() {
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
        if (offlineManager != null) {
            offlineManager!!.listOfflineRegions(object : ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<OfflineRegion>) {
                    if (offlineRegions.isNotEmpty()) { // delete the last item in the offlineRegions list which will be yosemite offline map
                        Log.d("DEBUG", "DEBUG MAP ID" + offlineRegions[offlineRegions.size - 1].id)
                        offlineRegions[offlineRegions.size - 1].delete(object : OfflineRegionDeleteCallback {
                            override fun onDelete() {
                                Toast.makeText(
                                        this@OfflineMapManagingActivity,
                                        "Map deleted",
                                        Toast.LENGTH_LONG
                                ).show()
                            }

                            override fun onError(error: String) {
                                Log.e("On delete error: %s", error)
                            }
                        })
                    }
                }

                override fun onError(error: String) {
                    Log.e("onListError: %s", error)
                }
            })
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    // Progress bar methods
    private fun startProgress() { // Start and show the progress bar
        isEndNotified = false
        progressBar!!.isIndeterminate = true
        progressBar!!.visibility = View.VISIBLE
    }

    private fun setPercentage(percentage: Int) {
        progressBar!!.isIndeterminate = false
        progressBar!!.progress = percentage
    }

    private fun endProgress(message: String) { // Don't notify more than once
        if (isEndNotified) {
            return
        }
        // Stop and hide the progress bar
        isEndNotified = true
        progressBar!!.isIndeterminate = false
        progressBar!!.visibility = View.GONE
        // Show a toast
        Toast.makeText(this@OfflineMapManagingActivity, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        // JSON encoding/decoding
        const val JSON_CHARSET = "UTF-8"
        const val JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME"
    }
}