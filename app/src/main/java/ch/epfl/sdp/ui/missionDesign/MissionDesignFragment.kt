package ch.epfl.sdp.ui.missionDesign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ch.epfl.sdp.R

class MissionDesignFragment : Fragment() {

    private lateinit var missionDeignViewModel: MissionDeignViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        missionDeignViewModel =
                ViewModelProviders.of(this).get(MissionDeignViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_mission_design, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        missionDeignViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Misson design fragment"
        })
        return root
    }
}
