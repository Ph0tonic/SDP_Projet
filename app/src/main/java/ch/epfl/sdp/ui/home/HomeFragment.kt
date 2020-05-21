package ch.epfl.sdp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.ui.MainActivity


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        updateCurrentGroup(v)
        return v
    }

    private fun updateCurrentGroup(v : View){
        var groupText = v.findViewById<TextView>(R.id.current_group_text)
        groupText?.text = MainApplication.applicationContext().getString(R.string.group_name_main_activity, MainActivity().getGroup())
    }
}
