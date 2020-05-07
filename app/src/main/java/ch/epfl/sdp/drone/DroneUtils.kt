package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission

object DroneUtils {
    private val missionItems = arrayListOf<Mission.MissionItem>()

    fun makeDroneMission(path: List<LatLng>, altitude: Float): DroneUtils {
        path.forEach { point ->
            missionItems.add(generateMissionItem(point.latitude, point.longitude, altitude))
        }
        return this
    }

    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double, altitude: Float): Mission.MissionItem {
        return Mission.MissionItem(
                latitudeDeg,
                longitudeDeg,
                altitude,
                10f,
                true, Float.NaN, Float.NaN,
                Mission.MissionItem.CameraAction.NONE, Float.NaN,
                1.0)
    }

    fun getMissionItems(): ArrayList<Mission.MissionItem> {
        return missionItems
    }
}
