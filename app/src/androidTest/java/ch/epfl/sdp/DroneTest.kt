package ch.epfl.sdp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneUtils
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DroneTest {

    companion object {
        const val SIGNAL_STRENGTH = 1.0
        private const val EPSILON = 1e-6
        private const val DEFAULT_ALTITUDE = 10f
        val someLocationsList = arrayListOf(
                LatLng(47.398979, 8.543434),
                LatLng(47.398279, 8.543934),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        )
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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
    fun missionTestDoesNotCrash() {
        Drone.currentMissionLiveData.value = null
        assertThat(Drone.currentMissionLiveData.value, `is`(nullValue()))

        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        // This assert prevent the app to crash in cash the mission has not been updated
        assertThat(Drone.currentMissionLiveData.value, `is`(notNullValue()))
        assertThat(Drone.currentMissionLiveData.value?.isEmpty(), `is`(false))
    }

    @Test
    fun canStartMissionAndReturnHome() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off

        Drone.currentPositionLiveData.postValue(expectedLatLng)
        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        Thread.sleep(10000) // let the drone move a bit

        Drone.returnHome()

        assertThat(Drone.currentMissionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = Drone.currentMissionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        if (currentLat == null || currentLong == null) {
            Assert.fail("Current location was null")
        }

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
    }


    @Test
    fun canStartMissionAndReturnToUser() {
        val expectedLatLng = LatLng(47.297428, 8.445369) //Position near the takeoff
        CentralLocationManager.currentUserPosition.postValue(expectedLatLng)

        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))

        Thread.sleep(10000)

        assertThat(CentralLocationManager.currentUserPosition.value, `is`(notNullValue()))
        Drone.returnUser()

        assertThat(Drone.currentMissionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = Drone.currentMissionLiveData.value?.get(0)

        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        if (currentLat == null || currentLong == null) {
            Assert.fail("Current location was null")
        }

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
    }
}