package ch.epfl.sdp.drone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data_manager.MainDataManager
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DroneTest {

    companion object {
        private const val SIGNAL_STRENGTH = 1.0
        private const val EPSILON = 1e-5
        private const val DEFAULT_ALTITUDE = 10f
        private const val DUMMY_GROUP_ID = "dummy_group_id"
        val someLocationsList = listOf(
                LatLng(47.398979, 8.543434),
                LatLng(47.398279, 8.543934),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        )
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        DroneInstanceMock.setupDefaultMocks()
    }

    @Test
    fun testSignal() {
        MainDataManager.goOffline()
        Drone.getSignalStrength = { SIGNAL_STRENGTH }
        assertThat(Drone.getSignalStrength(), closeTo(SIGNAL_STRENGTH, EPSILON))
        print(Drone.debugGetSignalStrength)
    }

    @Test
    fun missionTestDoesNotCrash() {
        Drone.missionLiveData.value = null
        assertThat(Drone.missionLiveData.value, `is`(nullValue()))

        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)

        // This assert prevent the app to crash in cash the mission has not been updated
        assertThat(Drone.missionLiveData.value, `is`(notNullValue()))
        assertThat(Drone.missionLiveData.value?.isEmpty(), `is`(false))
    }

    @Test
    fun canStartMissionAndReturnHome() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off

        Drone.positionLiveData.value = expectedLatLng
        Drone.homeLocationLiveData.value =
                Telemetry.Position(expectedLatLng.latitude, expectedLatLng.longitude, 400f, 50f)
        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)

        Drone.returnToHomeLocationAndLand()

        assertThat(Drone.missionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = Drone.missionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        assertThat(currentLat, `is`(notNullValue()))
        assertThat(currentLong, `is`(notNullValue()))

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
    }


    @Test
    fun canStartMissionAndReturnToUser() {
        val expectedLatLng = LatLng(47.297428, 8.445369) //Position near the takeoff
        CentralLocationManager.currentUserPosition.value = expectedLatLng

        Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)

        assertThat(CentralLocationManager.currentUserPosition.value, `is`(notNullValue()))
        Drone.returnToUserLocationAndLand()

        assertThat(Drone.missionLiveData.value?.isEmpty(), `is`(false))
        val returnToUserMission = Drone.missionLiveData.value?.get(0)

        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        assertThat(currentLat, `is`(notNullValue()))
        assertThat(currentLong, `is`(notNullValue()))

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
    }

    @Test
    fun canPauseMission() {
        Drone.isFlyingLiveData.value = true
        Drone.isMissionPausedLiveData.value = false

        Drone.startOrPauseMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)

        assertThat(Drone.isMissionPausedLiveData.value, `is`(true))
    }

    @Test
    fun canRestartMissionAfterPause() {
        Drone.isFlyingLiveData.value = true
        Drone.isMissionPausedLiveData.value = false

        Drone.startOrPauseMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)
        Drone.startOrPauseMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE), DUMMY_GROUP_ID)

        assertThat(Drone.isMissionPausedLiveData.value, `is`(false))
    }
}