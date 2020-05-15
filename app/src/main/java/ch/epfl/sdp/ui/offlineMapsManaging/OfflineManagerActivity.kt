package ch.epfl.sdp.ui.offlineMapsManaging

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.R
import ch.epfl.sdp.map.MapUtils
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import ch.epfl.sdp.ui.offlineMapsManaging.DownloadProgressBarUtils.deletingInProgress
import ch.epfl.sdp.ui.offlineMapsManaging.DownloadProgressBarUtils.downloadingInProgress
import ch.epfl.sdp.ui.offlineMapsManaging.DownloadProgressBarUtils.endProgress
import ch.epfl.sdp.ui.offlineMapsManaging.DownloadProgressBarUtils.startProgress
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.deleteOfflineRegion
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.getRegionName
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.showErrorAndToast
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import com.mapbox.mapboxsdk.offline.OfflineManager.CreateOfflineRegionCallback
import com.mapbox.mapboxsdk.offline.OfflineManager.ListOfflineRegionsCallback
import com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver
import org.json.JSONObject
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * Download, view, navigate to, and delete an offline region.
 *
 * Be careful, the maximum number of tiles a user can download is 6000
 * TODO : show error when user try to download more than the limit
 */
class OfflineManagerActivity : MapViewBaseActivity(), OnMapReadyCallback {
    private lateinit var mapboxMap: MapboxMap
    private lateinit var downloadButton: Button
    private lateinit var listButton: Button
    private lateinit var offlineManager: OfflineManager
    private lateinit var progressBar: ProgressBar

    private var regionSelected = 0

    companion object {
        // JSON encoding/decoding
        const val JSON_CHARSET = "UTF-8"
        const val JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME"
        const val MAX_ZOOM = 20.0  //  val maxZoom = map!!.maxZoomLevel //max Zoom is 25.5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_offline_manager, R.id.mapView)
        mapView.getMapAsync(this)

        mapView.contentDescription = getString(R.string.map_not_ready)

        // Assign progressBar for later use
        progressBar = findViewById(R.id.progress_bar)
        downloadButton = findViewById(R.id.download_button)
        listButton = findViewById(R.id.list_button)

        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(this@OfflineManagerActivity)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            mapboxMap.cameraPosition = MapUtils.getLastCameraState()

            // Used to detect when the map is ready in tests
            mapView.contentDescription = getString(R.string.map_ready)
        }
    }

    override fun onPause() {
        super.onPause()
        MapUtils.saveCameraPositionAndZoomToPrefs(mapboxMap.cameraPosition)
    }

    fun downloadRegionDialog(v: View) { // Set up download interaction. Display a dialog
        // when the user clicks download button and require
        // a user-provided region name
        val builder = AlertDialog.Builder(this@OfflineManagerActivity)

        val regionNameEdit = EditText(this@OfflineManagerActivity)
        regionNameEdit.hint = getString(R.string.set_region_name_hint)
        regionNameEdit.id = R.id.dialog_textfield_id

        //TODO: Use DialogueFragment
        // Build the dialog box
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                    val regionName = regionNameEdit.text.toString()
                    // Require a region name to begin the download.
                    // If the user-provided string is empty, display
                    // a toast message and do not begin download.
                    if (regionName.isEmpty()) {
                        Toast.makeText(applicationContext, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show()
                    } else { // Begin download process
                        downloadRegion(regionName)
                    }
                }
                .setNegativeButton(getString(R.string.dialog_negative_button)) { dialog, _ -> dialog.cancel() }
        // Display the dialog
        builder.show()
    }

    /**
     * Define offline region parameters, including bounds,
     * min/max zoom, and metadata
     */
    private fun downloadRegion(regionName: String) {
        startProgress(downloadButton, listButton, progressBar)
        // Create offline definition using the current
        // style and boundaries of visible map area
        mapboxMap.getStyle { style ->
            val definition = OfflineTilePyramidRegionDefinition(
                    style.uri,
                    mapboxMap.projection.visibleRegion.latLngBounds,
                    mapboxMap.cameraPosition.zoom, MAX_ZOOM,
                    this@OfflineManagerActivity.resources.displayMetrics.density)
            // Build a JSONObject using the user-defined offline region title,
            // convert it into string, and use it to create a metadata variable.
            // The metadata variable will later be passed to createOfflineRegion()
            val metadata = try {
                val jsonObject = JSONObject().put(JSON_FIELD_REGION_NAME, regionName)
                jsonObject.toString().toByteArray(charset(JSON_CHARSET))
            } catch (exception: Exception) {
                showErrorAndToast("Failed to encode metadata: " + exception.message)
                null
            }

            // Create the offline region and launch the download
            offlineManager.createOfflineRegion(definition, metadata!!, object : CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    launchDownload(offlineRegion)
                }

                override fun onError(error: String) {
                    showErrorAndToast("Error : $error")
                }
            })
        }
    }

    private fun launchDownload(offlineRegion: OfflineRegion) { // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        mapView.contentDescription = getString(R.string.map_downloading)
        offlineRegion.setObserver(object : OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) { // Compute a percentage
                val percentage = if (status.requiredResourceCount >= 0) 100.0 * status.completedResourceCount / status.requiredResourceCount else 0.0
                if (status.isComplete) { // Download complete
                    endProgress(downloadButton, listButton, progressBar)
                    mapView.contentDescription = getString(R.string.map_ready)
                    return
                } else if (status.isRequiredResourceCountPrecise) { // Switch to determinate state
                    downloadingInProgress(percentage.roundToInt(), progressBar)
                }
            }

            override fun onError(error: OfflineRegionError) {
                Timber.e("onError reason: %s", error.reason)
                showErrorAndToast("onError message: " + error.message)
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                showErrorAndToast("Mapbox tile count limit exceeded : $limit")
            }
        })
        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
    }

    fun downloadedRegionList(v: View) { // Build a region list when the user clicks the list button
        // Reset the region selected int to 0
        regionSelected = 0
        // Query the DB asynchronously
        offlineManager.listOfflineRegions(object : ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) { // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions.isEmpty()) {
                    Toast.makeText(applicationContext, getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show()
                    return
                }
                // Add all of the region names to a list
                val items = offlineRegions
                        .map { region ->
                            try {
                                getRegionName(region)
                            } catch (exception: java.lang.Exception) {
                                String.format(getString(R.string.region_name_error), region.id)
                            }
                        }
                        .toTypedArray<CharSequence>()
                // Build a dialog containing the list of regions
                showDialog(items, offlineRegions)
            }

            override fun onError(error: String) {
                showErrorAndToast("Error : $error")
            }
        })
    }

    private fun showDialog(items: Array<CharSequence>, offlineRegions: Array<OfflineRegion>) {
        return AlertDialog.Builder(this@OfflineManagerActivity)
                .setTitle(getString(R.string.navigate_title)).setSingleChoiceItems(items, 0) { _, which -> regionSelected = which }
                .setPositiveButton(getString(R.string.navigate_positive_button)) { _, _ ->
                    Toast.makeText(this@OfflineManagerActivity, items[regionSelected], Toast.LENGTH_LONG).show()
                    // Create new camera position
                    val definition = offlineRegions[regionSelected].definition
                    mapboxMap.cameraPosition = MapUtils.getCameraWithParameters(
                            definition.bounds.center,
                            definition.minZoom)
                }
                .setNeutralButton(getString(R.string.navigate_neutral_button_title)) { _, _ ->
                    // Make progressBar indeterminate and
                    // set it to visible to signal that
                    // the deletion process has begun
                    deletingInProgress(progressBar)
                    // Begin the deletion process
                    deleteOfflineRegion(offlineRegions[regionSelected], progressBar, mapView)
                }
                // When the user cancels, don't do anything.
                // The dialog will automatically close
                .setNegativeButton(getString(R.string.dialog_negative_button)
                ) { _, _ -> }.create().show()
    }
}