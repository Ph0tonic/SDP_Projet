package ch.epfl.sdp.ui.maps.offline

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ch.epfl.sdp.R
import ch.epfl.sdp.map.MapUtils
import ch.epfl.sdp.map.offline.DownloadProgressBarUtils
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.offline.OfflineRegion

class ListOfflineRegionDialogFragment(private val items: Array<CharSequence>,
                                      private val offlineRegions: Array<OfflineRegion>,
                                      private val mapboxMap: MapboxMap,
                                      private val progressBar: ProgressBar,
                                      private val mapView: MapView) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            var regionSelected = 0
            AlertDialog.Builder(it)
                    .setTitle(getString(R.string.navigate_title)).setSingleChoiceItems(items, 0) { _, which -> regionSelected = which }
                    .setPositiveButton(getString(R.string.navigate_positive_button)) { _, _ ->
                        Toast.makeText(it, items[regionSelected], Toast.LENGTH_LONG).show()
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
                        DownloadProgressBarUtils.deletingInProgress(progressBar)
                        // Begin the deletion process
                        OfflineRegionUtils.deleteOfflineRegion(offlineRegions[regionSelected], progressBar, mapView)
                    }
                    // When the user cancels, don't do anything.
                    // The dialog will automatically close
                    .setNegativeButton(getString(R.string.dialog_cancel)
                    ) { _, _ -> }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

