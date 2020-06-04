package ch.epfl.sdp.ui.maps.offline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.offline.OfflineRegionUtils
import ch.epfl.sdp.utils.OnItemClickListener
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus

class OfflineMapViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.map_selection_recyclerview_item, parent, false)){
    private var card: CardView? = itemView.findViewById(R.id.map_selection_item_card)
    private var nameView: TextView? = itemView.findViewById(R.id.offlineRegionName)
    private var propertyView: TextView? = itemView.findViewById(R.id.offlineRegionProperty)
    private lateinit var offlineRegion: OfflineRegion
    private lateinit var itemClickListener: OnItemClickListener<OfflineRegion>

    fun bind(region: OfflineRegion, itemClickListener: OnItemClickListener<OfflineRegion>) {
        nameView?.text = try {
            OfflineRegionUtils.getRegionName(region)
        } catch (exception: java.lang.Exception) {
            String.format(MainApplication.applicationContext().getString(R.string.region_name_error), region.id)
        }
        this.itemClickListener = itemClickListener

        card?.setOnClickListener{
            itemClickListener.onItemClicked(offlineRegion)
        }

        offlineRegion =  region

        region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
            /**
             * Receives the status
             *
             * @param status the offline region status
             */
            override fun onStatus(status: OfflineRegionStatus?) {
                propertyView?.text =  MainApplication.applicationContext().getString(R.string.offline_region_tiles, status?.completedResourceCount)
            }

            /**
             * Receives the error message
             *
             * @param error the error message
             */
            override fun onError(error: String?) {
                OfflineRegionUtils.showErrorAndToast("Error : $error")
            }
        })
    }
}