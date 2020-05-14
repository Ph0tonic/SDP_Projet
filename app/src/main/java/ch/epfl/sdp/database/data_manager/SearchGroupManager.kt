package ch.epfl.sdp.database.data_manager

import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository

class SearchGroupManager {

    private val searchGroupRepository = SearchGroupRepository()
    private val userRepository = UserRepository()
    private val markerRepository = MarkerRepository()
    private val heatmapRepository = HeatmapRepository()

    fun deleteSearchGroup(searchGroupId: String) {
        searchGroupRepository.removeSearchGroup(searchGroupId)
        userRepository.removeAllUserOfSearchGroup(searchGroupId)
        markerRepository.removeAllMarkersOfSearchGroup(searchGroupId)
        heatmapRepository.removeAllHeatmapsOfSearchGroup(searchGroupId)
    }

    fun createSearchGroup(name: String): String {
        TODO("Not implemented yet")
    }
}