package ch.epfl.sdp

import ch.epfl.sdp.drone.Drone
import io.mavsdk.mission.Mission

object DroneMissionExample {
    private val missionItems = arrayListOf<Mission.MissionItem>()

    fun makeDroneMission(): DroneMissionExample {
        addMissionItems()
        return this
    }

    fun startMission() {
        val drone = Drone.instance
        val isConnectedCompletable = drone.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError()
                .toCompletable()

        isConnectedCompletable
                .andThen(drone.mission.setReturnToLaunchAfterMission(true))
                .andThen(drone.mission.uploadMission(missionItems))
                .andThen(drone.action.arm())
                .andThen(drone.mission.startMission())
                .subscribe()
    }

    private fun addMissionItems() {
        missionItems.add(generateMissionItem(47.398039859999997, 8.5455725400000002))
        missionItems.add(generateMissionItem(47.398036222362471, 8.5450146439425509))
        missionItems.add(generateMissionItem(47.397825620791885, 8.5450092830163271))
        missionItems.add(generateMissionItem(47.397832880000003, 8.5455939999999995))
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
