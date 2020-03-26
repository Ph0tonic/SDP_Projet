package ch.epfl.sdp.ui.missionDesign

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.geometry.LatLng

class MissionDesignFragment : Fragment() {

    private lateinit var missionDesignViewModel: MissionDesignViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        missionDesignViewModel = ViewModelProvider(this).get(MissionDesignViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_mission_design, container, false)
        return root
    }
}
