package ch.epfl.sdp.utils

import androidx.preference.PreferenceManager
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.mission.OverflightStrategy
import ch.epfl.sdp.mission.SimpleQuadStrategy
import ch.epfl.sdp.mission.SpiralStrategy

object StrategyUtils {
    fun loadDefaultStrategyFromPreferences(): OverflightStrategy {
        val context = MainApplication.applicationContext()
        val strategyString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_overflight_strategy), "")
        return when (strategyString) {
            context.getString(R.string.pref_value_strategy_zigzag) ->
                SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE)
            context.getString(R.string.pref_value_strategy_spiral) ->
                SpiralStrategy(Drone.GROUND_SENSOR_SCOPE)
            else ->
                SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE)
        }
    }
}