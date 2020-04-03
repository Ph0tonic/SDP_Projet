package ch.epfl.sdp

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList
import java.util.concurrent.ThreadLocalRandom
import ch.epfl.sdp.DroneMission as DME

class DroneMissionTest {

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
        val positions = arrayListOf<LatLng>()
        positions.add(LatLng(47.398039859999997, 8.5455725400000002))
        positions.add(LatLng(47.398036222362471, 8.5450146439425509))
        positions.add(LatLng(47.397825620791885, 8.5450092830163271))
        positions.add(LatLng(47.397832880000003, 8.5455939999999995))

        val expectedMissionItems: ArrayList<Mission.MissionItem> = arrayListOf<Mission.MissionItem>()
        for(pos in positions){
            expectedMissionItems.add(ch.epfl.sdp.DroneMission.generateMissionItem(pos.latitude, pos.longitude))
        }

        val droneMissionExample =  DME.makeDroneMission(positions)
        val missionsItems = droneMissionExample.getMissionItems()

        for((i, expectedMission) in expectedMissionItems.withIndex()){
            Assert.assertTrue(missionEquality(expectedMission, missionsItems[i]))
        }
    }

    fun missionEquality (m1 : Mission.MissionItem, m2 : Mission.MissionItem) : Boolean{
        val lat = m1.latitudeDeg.equals(m2.latitudeDeg)
        val lon = m1.longitudeDeg.equals(m2.longitudeDeg)
        val alt = m1.relativeAltitudeM.equals(m2.relativeAltitudeM)
        val spe = m1.speedMS.equals(m2.speedMS)
        val fly = m1.isFlyThrough == m2.isFlyThrough
        val gim = m1.gimbalPitchDeg.equals(m2.gimbalPitchDeg)
        val yaw = m1.gimbalPitchDeg.equals(m2.gimbalPitchDeg)
        val cam = m1.cameraAction == m2.cameraAction
        val loi = m1.loiterTimeS.equals(m2.loiterTimeS)
        val gcpi = m1.cameraPhotoIntervalS.equals(m2.cameraPhotoIntervalS)
        return lat and lon and alt and spe and fly and gim and yaw and cam and loi and gcpi
    }
}


