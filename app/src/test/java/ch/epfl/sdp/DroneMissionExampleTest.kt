package ch.epfl.sdp

import io.mavsdk.mission.Mission
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList
import java.util.concurrent.ThreadLocalRandom
import ch.epfl.sdp.DroneMissionExample as DME

class DroneMissionExampleTest {

    @Test
    fun generateMissionItemTest(){
        val n = 100
        for (x in 0..n) {
            val randomLatitude = ThreadLocalRandom.current().nextDouble(-90.0, 90.0)
            val randomLongitude = ThreadLocalRandom.current().nextDouble(0.0, 180.0)
            var mission = Mission.MissionItem(
                    randomLatitude,
                    randomLongitude,
                    10f,
                    10f,
                    true, Float.NaN, Float.NaN,
                    Mission.MissionItem.CameraAction.NONE, Float.NaN,
                    1.0)
            var expectedMission = DME.generateMissionItem(randomLatitude, randomLongitude)
            Assert.assertTrue(missionEquality(expectedMission, mission))
        }
    }

    @Test
    fun makeDroneMissionTest(){
        val expectedMissionItems: ArrayList<Mission.MissionItem> = arrayListOf<Mission.MissionItem>()
        expectedMissionItems.add(ch.epfl.sdp.DroneMissionExample.generateMissionItem(47.398039859999997, 8.5455725400000002))
        expectedMissionItems.add(ch.epfl.sdp.DroneMissionExample.generateMissionItem(47.398036222362471, 8.5450146439425509))
        expectedMissionItems.add(ch.epfl.sdp.DroneMissionExample.generateMissionItem(47.397825620791885, 8.5450092830163271))
        expectedMissionItems.add(ch.epfl.sdp.DroneMissionExample.generateMissionItem(47.397832880000003, 8.5455939999999995))

        val droneMissionExample =  DME.makeDroneMission()
        val missionsItems = droneMissionExample.getMissionItems()

        for((i, expectedMission) in expectedMissionItems.withIndex()){
            Assert.assertTrue(missionEquality(expectedMission, missionsItems[i]))
        }

    }

    fun missionEquality (m1 : Mission.MissionItem, m2 : Mission.MissionItem) : Boolean{
        val lat = m1.getLatitudeDeg().equals(m2.getLatitudeDeg())
        val lon = m1.getLongitudeDeg().equals(m2.getLongitudeDeg())
        val alt = m1.getRelativeAltitudeM().equals(m2.getRelativeAltitudeM())
        val spe = m1.getSpeedMS().equals(m2.getSpeedMS())
        val fly = m1.getIsFlyThrough() == m2.getIsFlyThrough()
        val gim = m1.getGimbalPitchDeg().equals(m2.getGimbalPitchDeg())
        val yaw = m1.getGimbalPitchDeg().equals(m2.getGimbalPitchDeg())
        val cam = m1.getCameraAction() == m2.getCameraAction()
        val loi = m1.getLoiterTimeS().equals(m2.getLoiterTimeS())
        val gcpi = m1.getCameraPhotoIntervalS().equals(m2.getCameraPhotoIntervalS())
        return lat and lon and alt and spe and fly and gim and yaw and cam and loi and gcpi
    }
}


