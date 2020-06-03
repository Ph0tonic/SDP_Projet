package ch.epfl.sdp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data_manager.SearchGroupDataManager


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var groupButton: Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        groupButton = view.findViewById(R.id.search_group_selection_button)
        return view
    }

    override fun onResume() {
        super.onResume()
        updateGroupText()
    }

    private fun updateGroupText() {
        val groupId = PreferenceManager
                .getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(MainApplication.applicationContext().getString(R.string.pref_key_current_group_id), null)
        if (groupId == null) return
        else{
            SearchGroupDataManager().getGroupById(groupId.toString()).observeForever { group ->
                if (group !=null) {
                    groupButton.text = group.name
                }
            }
        }

    }
}
