package ch.epfl.sdp.ui.search_group.edition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.utils.OnItemClickListener

class UserRecyclerAdapter(
        private val list: List<UserData>,
        private val itemRemoveListener: OnItemClickListener<UserData>
) : RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val searchGroupData = list[position]
        holder.bind(searchGroupData, itemRemoveListener)
    }
}