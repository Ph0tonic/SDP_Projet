package ch.epfl.sdp.ui.maps.offline

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.DownloadProgressBarUtils
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.offline.OfflineRegion

class DeleteOfflineRegionConfirmDialogFragment(private val offlineRegion: OfflineRegion,
                                               private val progressBar: ProgressBar,
                                               private val mapView: MapView) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            AlertDialog.Builder(it)
                    .setTitle(getString(R.string.delete_this_offline_region))
                    .setPositiveButton(getString(R.string.navigate_neutral_button_title)) { _, _ ->
                        // Make progressBar indeterminate and set it to visible to signal that
                        // the deletion process has begun
                        DownloadProgressBarUtils.deletingInProgress(progressBar)
                        // Begin the deletion process
                        OfflineRegionUtils.deleteOfflineRegion(offlineRegion, progressBar, mapView)
                    }
                    // When the user cancels, don't do anything.
                    // The dialog will automatically close
                    .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

