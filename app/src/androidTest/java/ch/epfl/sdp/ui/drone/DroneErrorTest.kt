package ch.epfl.sdp.ui.drone

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class DroneErrorTest {

    companion object {
        private const val DEFAULT_ALTITUDE = 10f
        val someLocationsList = listOf(
                LatLng(47.398979, 8.543434),
                LatLng(47.398279, 8.543934),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        )
    }

    @Before
    fun before() {
        DroneInstanceMock.setupDefaultMocks()
        Mockito.reset(DroneInstanceMock.droneMission)

        //Mission mocks
        `when`(DroneInstanceMock.droneMission.pauseMission())
                .thenReturn(Completable.error(Throwable("Error PauseMission")))
        `when`(DroneInstanceMock.droneMission.setReturnToLaunchAfterMission(ArgumentMatchers.anyBoolean()))
                .thenReturn(Completable.error(Throwable("Error setReturnToLaunchAfterMission")))
        `when`(DroneInstanceMock.droneMission.uploadMission(ArgumentMatchers.any()))
                .thenReturn(Completable.error(Throwable("Error UploadMission")))
        `when`(DroneInstanceMock.droneMission.startMission())
                .thenReturn(Completable.error(Throwable("Error StartMission")))
        `when`(DroneInstanceMock.droneMission.clearMission())
                .thenReturn(Completable.error(Throwable("Error Clear Mission")))
    }

    @Test
    fun failToStartMissionResetMission() {
        runOnUiThread {
            Drone.startMission(DroneUtils.makeDroneMission(someLocationsList, DEFAULT_ALTITUDE))
        }

        assertThat(Drone.isMissionPausedLiveData.value, `is`(true))
        assertThat(Drone.missionLiveData.value, `is`(nullValue()))
    }

    @Test
    fun failToReturnHomeResetsMission() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off

        runOnUiThread {
            Drone.positionLiveData.value = expectedLatLng
            Drone.homeLocationLiveData.value =
                    Telemetry.Position(expectedLatLng.latitude, expectedLatLng.longitude, 400f, 50f)

            Drone.returnToHomeLocationAndLand()
        }
        assertThat(Drone.missionLiveData.value, `is`(nullValue()))
    }

    @Test
    fun failToRestartMissionResetsMissionStatus() {
        runOnUiThread {
            Drone.isFlyingLiveData.value = true
            Drone.isMissionPausedLiveData.value = true

            Drone.restartMission()
        }
        assertThat(Drone.isMissionPausedLiveData.value, `is`(true))
    }

    @Test
    fun failToPauseMissionResetsMissionStatus() {
        runOnUiThread {
            Drone.isFlyingLiveData.value = true
            Drone.isMissionPausedLiveData.value = false

            Drone.pauseMission()
        }
        assertThat(Drone.isMissionPausedLiveData.value, `is`(false))
    }
}