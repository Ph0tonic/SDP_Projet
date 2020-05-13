package ch.epfl.sdp.ui.search_group.edition

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.UserData

class UserViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.user_recyclerview_item, parent, false)) {
    private val name: TextView? = itemView.findViewById(R.id.user_recyclerview_name)

    fun bind(user: UserData) {

        //TODO change this to username
        name?.text = user.email
    }
}