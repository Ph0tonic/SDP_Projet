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
            var latitude = 90 //-90 to 90
            var mission = Mission.MissionItem(
                    randomLatitude,
                    randomLongitude,
                    10f,
                    10f,
                    true, Float.NaN, Float.NaN,
                    Mission.MissionItem.CameraAction.NONE, Float.NaN,
                    1.0)
            var expectedMission = DME.generateMissionItem(randomLatitude, randomLongitude)
            Assert.assertEquals(mission, expectedMission)
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

        Assert.assertEquals(expectedMissionItems, missionsItems)
    }
}