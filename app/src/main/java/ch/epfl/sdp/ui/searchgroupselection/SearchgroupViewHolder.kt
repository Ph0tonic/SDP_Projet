package ch.epfl.sdp.ui.searchgroupselection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData

class SearchgroupViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.searchgroup_selection_recyclerview_item, parent, false)) {
    private val name: TextView? = itemView.findViewById(R.id.searchGroupItemName)
    private val editButton: Button = itemView.findViewById(R.id.search_group_selection_edit_button)!!

    fun bind(searchGroup: SearchGroupData, clickListener: OnItemClickListener, onEditButtonClickListener: OnItemClickListener) {
        name?.text = searchGroup.name

        itemView.setOnClickListener{
            clickListener.onItemClicked(searchGroup)
        }

        editButton.setOnClickListener{
            onEditButtonClickListener.onItemClicked(searchGroup)
        }
    }
}