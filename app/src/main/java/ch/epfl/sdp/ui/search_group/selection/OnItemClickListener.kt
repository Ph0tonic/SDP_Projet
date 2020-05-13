package ch.epfl.sdp.ui.search_group.selection

import ch.epfl.sdp.database.data.SearchGroupData

interface OnItemClickListener {
    fun onItemClicked(searchGroupData: SearchGroupData)
}