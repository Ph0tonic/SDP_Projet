package ch.epfl.sdp.ui.searchgroupselection

import ch.epfl.sdp.database.data.SearchGroupData

interface OnItemClickListener {
    fun onItemClicked(searchGroupData: SearchGroupData)
}