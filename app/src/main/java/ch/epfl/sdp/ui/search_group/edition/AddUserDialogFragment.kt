package ch.epfl.sdp.ui.search_group.edition

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
                        userAddListener.addUser(view.findViewById<EditText>(R.id.email_address).text.toString(), role)
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        dialog.cancel()
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
