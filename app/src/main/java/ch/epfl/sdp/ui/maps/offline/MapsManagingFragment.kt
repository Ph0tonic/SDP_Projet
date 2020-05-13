package ch.epfl.sdp.ui.maps.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.R

class MapsManagingFragment : Fragment() {

    private lateinit var mapsManagingViewModel: MapsManagingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapsManagingViewModel = ViewModelProvider(this).get(MapsManagingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_maps_managing, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        mapsManagingViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = getString(R.string.maps_managing)
        })
        return root
    }
}
