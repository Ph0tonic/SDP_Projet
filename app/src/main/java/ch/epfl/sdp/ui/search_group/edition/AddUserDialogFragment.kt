package ch.epfl.sdp.ui.search_group.edition

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
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
class AddUserDialogFragment(private val role: Role, private val userAddListener: UserAddListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return this.activity.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_add_user, null)
            builder.setView(view)

            view.findViewById<Button>(R.id.dialog_add_user).setOnClickListener {
                val email = view.findViewById<EditText>(R.id.add_user_email_address).text.toString()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    userAddListener.addUser(email, role)
                    dismiss()
                } else {
                    view.findViewById<EditText>(R.id.add_user_email_address).error = getString(R.string.invalid_email_address)
                }
            }

            view.findViewById<Button>(R.id.dialog_cancel_add_user).setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
