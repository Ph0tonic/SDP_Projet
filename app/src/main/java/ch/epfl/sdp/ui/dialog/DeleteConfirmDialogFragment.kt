package ch.epfl.sdp.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ch.epfl.sdp.R

class DeleteConfirmDialogFragment(private val title: String, private val onDelete: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            AlertDialog.Builder(it)
                    .setTitle(title)
                    .setPositiveButton(getString(R.string.navigate_neutral_button_title)) { _, _ ->
                        onDelete()
                    }
                    // When the user cancels, don't do anything.
                    // The dialog will automatically close
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

