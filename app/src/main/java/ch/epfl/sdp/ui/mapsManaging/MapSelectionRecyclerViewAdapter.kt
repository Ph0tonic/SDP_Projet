package ch.epfl.sdp.ui.mapsManaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.SavedMap

class MapSelectionRecyclerViewAdapter(private val list: List<SavedMap>)
    : RecyclerView.Adapter<SavedMapViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedMapViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SavedMapViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SavedMapViewHolder, position: Int) {
        val savedMap: SavedMap = list[position]
        holder.bind(savedMap)
    }

    override fun getItemCount(): Int = list.size

}