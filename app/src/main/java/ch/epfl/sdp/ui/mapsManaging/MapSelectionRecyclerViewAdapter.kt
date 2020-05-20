package ch.epfl.sdp.ui.mapsManaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.offline.OfflineRegion

class MapSelectionRecyclerViewAdapter(private val regions: Array<OfflineRegion>)
    : RecyclerView.Adapter<OfflineMapViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineMapViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OfflineMapViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: OfflineMapViewHolder, position: Int) {
        val offlineRegion: OfflineRegion = regions[position]
        holder.bind(offlineRegion)
    }

    override fun getItemCount(): Int = regions.size

}