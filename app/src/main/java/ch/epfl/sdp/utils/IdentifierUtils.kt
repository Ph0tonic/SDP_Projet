package ch.epfl.sdp.utils

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ch.epfl.sdp.MainApplication
import java.util.*

object IdentifierUtils {
    private var uniqueID: String? = null
    private const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"

    @Synchronized
    fun id(): String {
        if (uniqueID == null) {
            val sharedPrefs: SharedPreferences = MainApplication.applicationContext().getSharedPreferences(
                    PREF_UNIQUE_ID, MODE_PRIVATE)
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.apply()
            }
        }
        return uniqueID!!
    }
}