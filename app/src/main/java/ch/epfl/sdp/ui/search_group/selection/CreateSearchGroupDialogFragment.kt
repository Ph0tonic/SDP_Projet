package ch.epfl.sdp.ui.search_group.selection

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role

/**
 * A simple [Fragment] subclass.
 * Use the [CreateSearchGroupDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateSearchGroupDialogFragment(private val createSearchGroupListener: CreateSearchGroupListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return this.activity.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_create_searchgroup, null)
            builder.setView(view)

            view.findViewById<Button>(R.id.dialog_create_searchgroup).setOnClickListener {
                val name = view.findViewById<EditText>(R.id.create_search_group_name).text.toString()
                if (name.isNotEmpty()) {
                    createSearchGroupListener.createGroup(name)
                    dismiss()
                } else {
                    view.findViewById<EditText>(R.id.create_search_group_name).error = getString(R.string.search_group_name_cannot_be_empty)
                }
            }

            view.findViewById<Button>(R.id.dialog_cancel_create_searchgroup).setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
