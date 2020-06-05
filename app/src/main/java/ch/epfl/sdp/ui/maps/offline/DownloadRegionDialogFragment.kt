package ch.epfl.sdp.ui.maps.offline

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R

class DownloadRegionDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            // Use the Builder class for convenient dialog construction
            // Set up download interaction. Display a dialog
            // when the user clicks download button and require
            // a user-provided region name
            val regionNameEdit = EditText(it)
            regionNameEdit.hint = getString(R.string.set_region_name_hint)
            regionNameEdit.id = R.id.dialog_textfield_id

            val builder = AlertDialog.Builder(it)

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
                            Toast.makeText(MainApplication.applicationContext(), getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show()
                        } else { // Begin download process
                            (activity as OfflineManagerActivity).prepareAndLaunchDownload(regionName)
                        }
                    }
                    .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ -> dialog.cancel() }
            // Display the dialog
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
