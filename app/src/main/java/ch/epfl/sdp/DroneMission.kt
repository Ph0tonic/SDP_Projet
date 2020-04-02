package ch.epfl.sdp

import ch.epfl.sdp.drone.Drone
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.mission.Mission

object DroneMission {
    private val missionItems = arrayListOf<Mission.MissionItem>()

    fun makeDroneMission(path: List<LatLng>): DroneMission {
        addMissionItems(path)
        return this
    }

    fun startMission(){
        var drone = Drone.instance
        var isConnectedCompletable = drone.getCore().getConnectionState()
                .filter{state -> state.getIsConnected()}
                .firstOrError()
                .toCompletable()

        isConnectedCompletable
                .andThen(drone.mission.setReturnToLaunchAfterMission(true))
                .andThen(drone.mission.uploadMission(missionItems))
                .andThen(drone.action.arm())
                .andThen(drone.mission.startMission())
                .subscribe()
    }

    private fun addMissionItems(path: List<LatLng>) {
        for(point in path) missionItems.add(generateMissionItem(point.latitude,point.longitude))
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

    fun getMissionItems(): ArrayList<Mission.MissionItem> {
        return missionItems
    }
}
