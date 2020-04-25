package ch.epfl.sdp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.drone.Drone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DroneTest {

    companion object{
        var DOUBLE_TEST = 1.0
    }


    @get:Rule
    var mActivityRule = ActivityTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately


    @Test
    fun testSignal() {
        Drone.getSignalStrength = { DOUBLE_TEST }
        assert(Drone.getSignalStrength() == DOUBLE_TEST)
        print(Drone.debugGetSignalStrength)
    }

    @Test
    fun missionTestDoesNotCrashes(){
        Drone.startMission(arrayListOf())
    }

}