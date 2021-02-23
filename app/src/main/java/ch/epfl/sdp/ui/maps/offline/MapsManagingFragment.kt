package ch.epfl.sdp.ui.maps.offline

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import ch.epfl.sdp.utils.OnItemClickListener
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion

class MapsManagingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mapsManagingViewModel: MapsManagingViewModel
    private lateinit var offlineManager: OfflineManager
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        mapsManagingViewModel = ViewModelProvider(this).get(MapsManagingViewModel::class.java)
        offlineManager = OfflineManager.getInstance(MainApplication.applicationContext())
        val view :View = inflater.inflate(R.layout.fragment_maps_managing, container, false)
        recyclerView = view.findViewById(R.id.mapSelectionRecyclerview)
        return view
    }

    override fun onStart() {
        super.onStart()
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) { // Check result. If no regions have been

                // RecyclerView node initialized here
                recyclerView.apply {
                    // set a LinearLayoutManager to handle Android
                    // RecyclerView behavior
                    layoutManager = LinearLayoutManager(activity)
                    // set the custom adapter to the RecyclerView
                    adapter = MapSelectionRecyclerViewAdapter(offlineRegions, object : OnItemClickListener<OfflineRegion> {
                        override fun onItemClicked(item: OfflineRegion) {
                            openExistingRegion(item)
                        }
                    })
                }
                view?.findViewById<TextView>(R.id.no_offline_map)?.visibility = if (offlineRegions.isNotEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun onError(error: String) {
                OfflineRegionUtils.showErrorAndToast("Error : $error")
            }
        })
    }

    private fun openExistingRegion(offlineRegion: OfflineRegion) {
        val context = MainApplication.applicationContext()
        val intent = Intent(context, OfflineManagerActivity::class.java)
        intent.putExtra(getString(R.string.intent_key_show_delete_button), true)
        intent.putExtra(getString(R.string.intent_key_offline_region_id), offlineRegion.id)
        startActivity(intent)
    }
}
