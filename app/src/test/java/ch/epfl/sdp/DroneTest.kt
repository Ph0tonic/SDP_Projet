package ch.epfl.sdp

import ch.epfl.sdp.drone.Drone
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers.closeTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test

class DroneTest {

    private fun startMissionTest(): LatLng? {
        val droneBeforeTakeOffPosition = LatLng(47.397428, 8.545369)
        DroneMission.makeDroneMission(
                arrayListOf(
                LatLng(47.398979,  8.543434),
                LatLng(47.398279, 8.543434),
                LatLng(47.397426, 8.544867),
                LatLng(47.397026, 8.543067)
        ))


        Drone.currentPositionLiveData.postValue(droneBeforeTakeOffPosition)
        Drone.startMission(DroneMission.getMissionPlan())

        return droneBeforeTakeOffPosition
    }

    @Test
    fun canStartMissionAndReturnHome(){
        val expectedLatLng = startMissionTest()
        Thread.sleep(10000) // let the drone move a bit

        Drone.returnHome()

        val returnToUserMission = Drone.currentMissionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        if (currentLat == null || currentLong == null){
            Assert.fail("Current location was null")
        }

        //compare both position
        if (expectedLatLng != null) {
            assertThat(currentLat, closeTo(expectedLatLng.latitude, 0.01))
            assertThat(currentLong, closeTo(expectedLatLng.longitude, 0.01))
        }else{
            Assert.fail("Expected location was null")
        }
    }

    @Test
    fun canStartMissionAndReturnToUser(){
        val expectedLatLng = CentralLocationManager.currentUserPosition.value

        startMissionTest()

        Thread.sleep(10000)

        Drone.returnUser()

        val returnToUserMission = Drone.currentMissionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        if (currentLat == null || currentLong == null){
            Assert.fail("Current location was null")
        }

        //compare both position
        if (expectedLatLng != null) {
            assertThat(currentLat, closeTo(expectedLatLng.latitude, 0.01))
            assertThat(currentLong, closeTo(expectedLatLng.longitude, 0.01))
        }else{
            Assert.fail("Expected location was null")
        }
    }
}


