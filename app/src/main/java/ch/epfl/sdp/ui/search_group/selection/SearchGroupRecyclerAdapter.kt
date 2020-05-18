package ch.epfl.sdp.ui.search_group.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.ui.search_group.OnItemClickListener

class SearchGroupRecyclerAdapter(
        private val list: List<SearchGroupData>,
        private val itemClickListener: OnItemClickListener<SearchGroupData>,
        private val editButtonClickListener: OnItemClickListener<SearchGroupData>
) : RecyclerView.Adapter<SearchgroupViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchgroupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SearchgroupViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchgroupViewHolder, position: Int) {
        val searchGroupData = list[position]
        holder.bind(searchGroupData, itemClickListener, editButtonClickListener)
    }
}