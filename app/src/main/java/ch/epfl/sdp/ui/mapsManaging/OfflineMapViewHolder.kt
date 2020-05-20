package ch.epfl.sdp.ui.mapsManaging

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import com.mapbox.mapboxsdk.offline.OfflineRegion

class OfflineMapViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.map_selection_recyclerview_item, parent, false)) {
    private var mTitleView: TextView?
    private var mOtherView: TextView?

    init {
        mTitleView = itemView.findViewById(R.id.savedMapName)
        mOtherView = itemView.findViewById(R.id.savedMapOtherProperty)
    }

    fun bind(region: OfflineRegion) {
        mTitleView?.text = try {
            OfflineRegionUtils.getRegionName(region)
        } catch (exception: java.lang.Exception) {
            String.format(MainApplication.applicationContext().getString(R.string.region_name_error), region.id)
        }
        mOtherView?.text = "Used space: 213.34 Mb"
    }

}