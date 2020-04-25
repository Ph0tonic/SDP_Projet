package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission.MissionItem
import io.mavsdk.mission.Mission.MissionPlan

object DroneUtils {
    private lateinit var missionPlan: MissionPlan

    fun makeDroneMission(path: List<LatLng>) : MissionPlan {
        val missionItems = arrayListOf<MissionItem>()
        path.forEach { point ->
            missionItems.add(generateMissionItem(point.latitude, point.longitude))
        }

        missionPlan = MissionPlan(missionItems)
        return missionPlan
    }

    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double): MissionItem {
        return MissionItem(
                latitudeDeg,
                longitudeDeg,
                10f,
                10f,
                true, Float.NaN, Float.NaN,
                MissionItem.CameraAction.NONE, Float.NaN,
                1.0)
    }

    fun getMissionPlan(): MissionPlan {
        return missionPlan
    }
}
