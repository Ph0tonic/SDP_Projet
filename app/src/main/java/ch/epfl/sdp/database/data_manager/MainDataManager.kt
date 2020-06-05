package ch.epfl.sdp.database.data_manager

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.OfflineHeatmapDao
import ch.epfl.sdp.database.dao.OfflineMarkerDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.utils.Auth

object MainDataManager {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val OFFLINE_MODE_USER_ID = "offline_user_id"

    private var cachedUserIdWhileOffline: String? = null
    private var cachedUserRoleWhileOffline: Role = Role.RESCUER

    private var offlineMode = false

    private val offlineHeatmapDao = OfflineHeatmapDao()
    private val offlineMarkerDao = OfflineMarkerDao()

    val groupId: MutableLiveData<String?> = MutableLiveData(null)
    val role: MutableLiveData<Role> = MutableLiveData(Role.RESCUER)

    init {
        groupId.value = PreferenceManager
                .getDefaultSharedPreferences(applicationContext())
                .getString(applicationContext().getString(R.string.pref_key_current_group_id), null)
    }

    fun selectSearchGroup(groupId: String, role: Role) {
        PreferenceManager
                .getDefaultSharedPreferences(applicationContext())
                .edit()
                .putString(applicationContext().getString(R.string.pref_key_current_group_id), groupId)
                .apply()
        this.groupId.value = groupId
        this.role.value = role
    }

    fun goOffline() {
        if (!offlineMode) {
            HeatmapRepository.daoProvider = { offlineHeatmapDao }
            MarkerRepository.daoProvider = { offlineMarkerDao }
            cachedUserIdWhileOffline = groupId.value
            cachedUserRoleWhileOffline = role.value!!
        }
        if (Auth.accountId.value == null) {
            Auth.accountId.value = OFFLINE_MODE_USER_ID
        }
        groupId.value = OFFLINE_MODE_USER_ID
        role.value = Role.OPERATOR
        offlineMode = true
    }

    fun goOnline() {
        if (offlineMode) {
            HeatmapRepository.daoProvider = HeatmapRepository.DEFAULT_DAO
            MarkerRepository.daoProvider = MarkerRepository.DEFAULT_DAO
            groupId.value = cachedUserIdWhileOffline
            role.value = cachedUserRoleWhileOffline
        }
        offlineMode = false
    }
}