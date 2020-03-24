package ch.epfl.sdp.ui.maps.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ch.epfl.sdp.R
import ch.epfl.sdp.SavedMap
import ch.epfl.sdp.ui.mapsManaging.MapSelectionRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_maps_managing.*

class MapsManagingFragment : Fragment() {

    private lateinit var mapsManagingViewModel: MapsManagingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapsManagingViewModel = ViewModelProvider(this).get(MapsManagingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_maps_managing, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_slideshow)
        mapsManagingViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = getString(R.string.maps_managing)
        })
            textView.text = "Maps managing fragment\n(get offline maps here)"
        })*/
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapNames: ArrayList<SavedMap> = ArrayList()
        mapNames.add(SavedMap("Matterhorn nord"))
        mapNames.add(SavedMap("Matterhorn sud"))
        mapNames.add(SavedMap("Diablerets"))
        mapNames.add(SavedMap("Verbier sud"))
        mapNames.add(SavedMap("Verbier ouest"))
        mapNames.add(SavedMap("Glacier 3000"))
        mapNames.add(SavedMap("Moleson"))
        mapNames.add(SavedMap("Montblanc"))
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        mapSelectionRecyclerview.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(activity)
            // set the custom adapter to the RecyclerView
            adapter = MapSelectionRecyclerViewAdapter(mapNames)
        }
    }
}
