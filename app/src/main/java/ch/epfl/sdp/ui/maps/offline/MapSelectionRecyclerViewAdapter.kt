package ch.epfl.sdp.ui.maps.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.utils.OnItemClickListener
import com.mapbox.mapboxsdk.offline.OfflineRegion

class MapSelectionRecyclerViewAdapter(
        private val regions: Array<OfflineRegion>,
        private val itemClickListener: OnItemClickListener<OfflineRegion>)
    : RecyclerView.Adapter<OfflineMapViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineMapViewHolder {
        return OfflineMapViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: OfflineMapViewHolder, position: Int) {
        holder.bind(regions[position], itemClickListener)
    }

    override fun getItemCount(): Int = regions.size
}