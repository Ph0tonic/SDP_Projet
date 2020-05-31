package ch.epfl.sdp.ui.maps.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import kotlinx.android.synthetic.main.fragment_maps_managing.*

class MapsManagingFragment : Fragment() {

    private lateinit var mapsManagingViewModel: MapsManagingViewModel
    private lateinit var offlineManager: OfflineManager
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapsManagingViewModel = ViewModelProvider(this).get(MapsManagingViewModel::class.java)
        offlineManager = OfflineManager.getInstance(MainApplication.applicationContext())
        return inflater.inflate(R.layout.fragment_maps_managing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) { // Check result. If no regions have been
                // RecyclerView node initialized here
                mapSelectionRecyclerview.apply {
                    // set a LinearLayoutManager to handle Android
                    // RecyclerView behavior
                    layoutManager = LinearLayoutManager(activity)
                    // set the custom adapter to the RecyclerView
                    adapter = MapSelectionRecyclerViewAdapter(offlineRegions)
                }
            }

            override fun onError(error: String) {
                OfflineRegionUtils.showErrorAndToast("Error : $error")
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }
}
