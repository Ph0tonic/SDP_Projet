package ch.epfl.sdp.ui.maps

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import ch.epfl.sdp.R
import ch.epfl.sdp.drone.Drone

class ReturnDroneDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Build the dialog box
            builder.setTitle(getString(R.string.return_drone_dialog_title))
                    .setPositiveButton(getString(R.string.return_drone_dialog_option_home)) { _, _ ->
                        tryReturnHome(it)
                    }
                    .setNeutralButton(R.string.return_drone_dialog_option_user) { _, _ ->
                        try {
                            Drone.returnToUserLocationAndLand()
                            // If the user position is not available, we show it to user and return home instead
                        } catch (e: java.lang.IllegalStateException) {
                            Toast.makeText(it, it.getText(R.string.drone_user_error), Toast.LENGTH_SHORT).show()
                            tryReturnHome(it)
                        }
                    }
                    .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ -> dialog.cancel() }
            // Display the dialog
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun tryReturnHome(activity : FragmentActivity){
        try {
            Drone.returnToHomeLocationAndLand()
        } catch (e: java.lang.IllegalStateException) {
            Toast.makeText(activity, activity.getText(R.string.drone_home_error), Toast.LENGTH_SHORT).show()
        }
    }
}
