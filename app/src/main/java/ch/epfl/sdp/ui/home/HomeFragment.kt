package ch.epfl.sdp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.DroneMissionExample
import ch.epfl.sdp.R
import ch.epfl.sdp.MapActivity

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val button: Button = root.findViewById(R.id.display_map)
        button.setOnClickListener {
            startActivity(Intent(context, MapActivity::class.java))
        }

        val startMissionButton: Button = root.findViewById(R.id.startMissionButton)
        startMissionButton.setOnClickListener {
            val dme = DroneMissionExample.makeDroneMission()
            dme.startMission()
        }
        return root
    }
}
