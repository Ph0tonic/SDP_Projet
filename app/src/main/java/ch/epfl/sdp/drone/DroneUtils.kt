package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission.MissionItem
import io.mavsdk.mission.Mission.MissionPlan

object DroneUtils {

    fun makeDroneMission(path: List<LatLng>, altitude: Float): MissionPlan {
        return MissionPlan(path.map { point ->
            generateMissionItem(point.latitude, point.longitude, altitude)
        })
    }

    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double, altitude: Float): MissionItem {
        return MissionItem(
                latitudeDeg,
                longitudeDeg,
                altitude,
                10f,
                true, Float.NaN, Float.NaN,
                MissionItem.CameraAction.NONE, Float.NaN,
                1.0)
    }
}
