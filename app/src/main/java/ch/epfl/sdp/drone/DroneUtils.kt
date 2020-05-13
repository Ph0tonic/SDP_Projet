package ch.epfl.sdp.drone

import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission

object DroneUtils {
    fun makeDroneMission(path: List<LatLng>): List<Mission.MissionItem> {
        return path.map {
            generateMissionItem(it.latitude, it.longitude)
        }
    }

    fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double): Mission.MissionItem {
        return Mission.MissionItem(
                latitudeDeg,
                longitudeDeg,
                10f,
                10f,
                true, Float.NaN, Float.NaN,
                Mission.MissionItem.CameraAction.NONE, Float.NaN,
                1.0)
    }
}
