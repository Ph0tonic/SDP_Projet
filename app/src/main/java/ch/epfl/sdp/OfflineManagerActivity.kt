package ch.epfl.sdp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.map.MapUtils
import ch.epfl.sdp.ui.maps.MapViewBaseActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import com.mapbox.mapboxsdk.offline.OfflineManager.CreateOfflineRegionCallback
import com.mapbox.mapboxsdk.offline.OfflineManager.ListOfflineRegionsCallback
import com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionDeleteCallback
import com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver
import org.json.JSONObject
import timber.log.Timber
import java.nio.charset.Charset
import kotlin.math.roundToInt

/**
 * Download, view, navigate to, and delete an offline region.
 *
 * Be careful, the maximum number of tiles a user can download is 6000
 * TODO : show error when user try to download more than the limit
 */
class OfflineManagerActivity : MapViewBaseActivity(), OnMapReadyCallback {
    private var isEndNotified = false

    private lateinit var map: MapboxMap
    private lateinit var progressBar: ProgressBar
    private lateinit var downloadButton: Button
    private lateinit var listButton: Button
    private lateinit var offlineManager: OfflineManager

    private var regionSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.initMapView(savedInstanceState, R.layout.activity_offline_manager, R.id.mapView)
        mapView.getMapAsync(this)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            // Assign progressBar for later use
            progressBar = findViewById(R.id.progress_bar)
            // Set up the offlineManager
            offlineManager = OfflineManager.getInstance(this@OfflineManagerActivity)
            // Bottom navigation bar button clicks are handled here.
            // Download offline button
            downloadButton = findViewById(R.id.download_button)
            downloadButton.setOnClickListener { downloadRegionDialog() }
            // List offline regions
            listButton = findViewById(R.id.list_button)
            listButton.setOnClickListener { downloadedRegionList() }
        }
        mapboxMap.cameraPosition =  MapUtils.getLastCameraState()
    }

    private fun downloadRegionDialog() { // Set up download interaction. Display a dialog
        // when the user clicks download button and require
        // a user-provided region name
        val builder = AlertDialog.Builder(this@OfflineManagerActivity)

        val regionNameEdit = EditText(this@OfflineManagerActivity)
        regionNameEdit.hint = getString(R.string.set_region_name_hint)
        regionNameEdit.id = R.id.dialog_textfield_id

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
                        Toast.makeText(this@OfflineManagerActivity, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show()
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
        startProgress()
        // Create offline definition using the current
        // style and boundaries of visible map area
        map.getStyle { style ->
            val maxZoom = 20.0  //  val maxZoom = map!!.maxZoomLevel //max Zoom is 25.5
            val definition = OfflineTilePyramidRegionDefinition(
                    style.uri,
                    map.projection.visibleRegion.latLngBounds,
                    map.cameraPosition.zoom, maxZoom,
                    this@OfflineManagerActivity.resources.displayMetrics.density)
            // Build a JSONObject using the user-defined offline region title,
            // convert it into string, and use it to create a metadata variable.
            // The metadata variable will later be passed to createOfflineRegion()
            val metadata: ByteArray?
            metadata = try {
                val jsonObject = JSONObject().put(JSON_FIELD_REGION_NAME, regionName)
                jsonObject.toString().toByteArray(charset(JSON_CHARSET))
            } catch (exception: Exception) {
                Timber.e("Failed to encode metadata: %s", exception.message)
                null
            }
            // Create the offline region and launch the download
            offlineManager.createOfflineRegion(definition, metadata!!, object : CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    launchDownload(offlineRegion)
                }

                override fun onError(error: String) {
                    Timber.e("Error: %s", error)
                }
            })
        }
    }

    private fun launchDownload(offlineRegion: OfflineRegion) { // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegion.setObserver(object : OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) { // Compute a percentage
                val percentage = if (status.requiredResourceCount >= 0) 100.0 * status.completedResourceCount / status.requiredResourceCount else 0.0
                if (status.isComplete) { // Download complete
                    endProgress(getString(R.string.end_progress_success))
                    return
                } else if (status.isRequiredResourceCountPrecise) { // Switch to determinate state
                    progressBar.isIndeterminate = false
                    progressBar.progress = percentage.roundToInt()
                }
            }

            override fun onError(error: OfflineRegionError) {
                Timber.e("onError reason: %s", error.reason)
                Timber.e("onError message: %s", error.message)
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                Timber.e("Mapbox tile count limit exceeded: %s", limit)
            }
        })
        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
    }

    private fun downloadedRegionList() { // Build a region list when the user clicks the list button
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
                        .map { region -> getRegionName(region) }
                        .toTypedArray<CharSequence>()
                // Build a dialog containing the list of regions
                showDialog(items, offlineRegions)
            }

            override fun onError(error: String) { Timber.e("Error: %s", error) }
        })
    }

    private fun showDialog(items: Array<CharSequence>, offlineRegions: Array<OfflineRegion>) {
        return AlertDialog.Builder(this@OfflineManagerActivity)
                .setTitle(getString(R.string.navigate_title)).setSingleChoiceItems(items, 0) { _, which -> regionSelected = which }
                .setPositiveButton(getString(R.string.navigate_positive_button)) { _, _ ->
                    Toast.makeText(this@OfflineManagerActivity, items[regionSelected], Toast.LENGTH_LONG).show()
                    // Create new camera position
                    val definition = offlineRegions[regionSelected].definition
                    map.cameraPosition = MapUtils.getCameraWithParameters(
                            LatLng(definition.bounds.latitudeSpan, definition.bounds.longitudeSpan),
                            definition.minZoom)
                }
                .setNeutralButton(getString(R.string.navigate_neutral_button_title)) { _, _ ->
                    // Make progressBar indeterminate and
                    // set it to visible to signal that
                    // the deletion process has begun
                    progressBar.isIndeterminate = true
                    progressBar.visibility = View.VISIBLE
                    // Begin the deletion process
                    deleteOfflineRegion(offlineRegions[regionSelected])
                }
                // When the user cancels, don't do anything.
                // The dialog will automatically close
                .setNegativeButton(getString(R.string.navigate_negative_button_title)
                ) { _, _ -> }.create().show()
    }

    private fun deleteOfflineRegion(offRegion: OfflineRegion) {
        offRegion.delete(object : OfflineRegionDeleteCallback {
            override fun onDelete() { // Once the region is deleted, remove the
                // progressBar and display a toast
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                Toast.makeText(applicationContext, getString(R.string.toast_region_deleted),
                        Toast.LENGTH_LONG).show()
            }

            override fun onError(error: String) {
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                Timber.e("Error: %s", error)
            }
        })
    }

    // Get the region name from the offline region metadata
    private fun getRegionName(offlineRegion: OfflineRegion): String {
        val regionName: String
        regionName = try {
            JSONObject(String(offlineRegion.metadata, Charset.forName(JSON_CHARSET)))
                    .getString(JSON_FIELD_REGION_NAME)
        } catch (exception: Exception) {
            Timber.e("Failed to decode metadata: %s", exception.message)
            String.format(getString(R.string.region_name), offlineRegion.id)
        }
        return regionName
    }

    // Progress bar methods
    private fun startProgress() { // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        isEndNotified = false
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    private fun endProgress(message: String) { // Don't notify more than once
        if (isEndNotified) return

        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        isEndNotified = true
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        Toast.makeText(this@OfflineManagerActivity, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        // JSON encoding/decoding
        const val JSON_CHARSET = "UTF-8"
        const val JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME"
    }
}