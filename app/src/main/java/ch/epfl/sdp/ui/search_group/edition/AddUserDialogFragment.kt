package ch.epfl.sdp.ui.search_group.edition

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role

/**
 * A simple [Fragment] subclass.
 * Use the [AddUserDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddUserDialogFragment(val role: Role, val userAddListener: UserAddListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_add_user, null)
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.add_a_user) { dialog, id ->
                        //TODO Validate email
                        userAddListener.addUser(view.findViewById<EditText>(R.id.add_user_email_address).text.toString(), role)
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        dialog.cancel()
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
