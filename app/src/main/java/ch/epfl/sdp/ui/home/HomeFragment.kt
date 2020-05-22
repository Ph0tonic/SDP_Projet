package ch.epfl.sdp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.FirebaseGroupDao
import ch.epfl.sdp.database.data_manager.SearchGroupDataManager
import ch.epfl.sdp.ui.MainActivity


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var groupText: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        groupText = view.findViewById(R.id.current_group_text)
        val groupID = PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(getString(R.string.pref_key_current_group_id), "No group")
        //groupText.text = MainApplication.applicationContext().getString(R.string.group_name_main_activity,SearchGroupDataManager().getGroupById(MainActivity().getCurrentGroupID()).value?.name)
        groupText.text = MainApplication.applicationContext().getString(R.string.group_name_main_activity,FirebaseGroupDao().getGroupById(groupID.toString()).value?.name )
        return view
    }

    override fun onResume() {
        super.onResume()
        val groupID = PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(getString(R.string.pref_key_current_group_id), "No group")
        //groupText.text = MainApplication.applicationContext().getString(R.string.group_name_main_activity,SearchGroupDataManager().getGroupById(MainActivity().getCurrentGroupID()).value?.name)
        groupText.text = MainApplication.applicationContext().getString(R.string.group_name_main_activity,FirebaseGroupDao().getGroupById(groupID.toString()).value?.name )
    }

}
