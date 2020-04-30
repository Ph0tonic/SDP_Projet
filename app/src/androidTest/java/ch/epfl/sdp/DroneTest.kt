package ch.epfl.sdp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.MapActivityTest.Companion.EPSILON
import ch.epfl.sdp.drone.Drone
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DroneTest {

    companion object {
        const val SIGNAL_STRENGTH = 1.0
    }

    @get:Rule
    var mActivityRule = ActivityTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Test
    fun testSignal() {
        Drone.getSignalStrength = { SIGNAL_STRENGTH }
        assertThat(Drone.getSignalStrength(), closeTo(SIGNAL_STRENGTH, EPSILON))
        print(Drone.debugGetSignalStrength)
    }

    @Test
    fun missionTestDoesNotCrashes() {
        assertThat(Drone.currentMissionLiveData.value, `is`(nullValue()))
        val someLocationsList = arrayListOf(
                LatLng(47.398979, 8.543434),
                LatLng(47.398279, 8.543934),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        )
        Drone.startMission(DroneMission.makeDroneMission(someLocationsList).getMissionItems())
        assertThat(Drone.currentMissionLiveData.value?.isEmpty(), `is`(false))
    }

}