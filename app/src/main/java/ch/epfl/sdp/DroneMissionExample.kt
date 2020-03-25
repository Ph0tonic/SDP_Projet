package ch.epfl.sdp

import io.mavsdk.System
import io.mavsdk.mission.Mission

object DroneMissionExample {
    //must be IP address where the mavsdk_server is running
    //private val BACKEND_IP_ADDRESS = "127.0.0.1"
    private val BACKEND_IP_ADDRESS = "10.0.2.2"
    //private val BACKEND_IP_ADDRESS = "10.0.2.15"

    private val missionItems = arrayListOf<Mission.MissionItem>()
    private var drone = System(BACKEND_IP_ADDRESS, 50051)

    fun makeDroneMission(): DroneMissionExample {
        addMissionItems()
        return this
    }

    fun startMission(){
       // drone.getAction().arm().subscribe()
        drone.getMission()
                .setReturnToLaunchAfterMission(true)
                .andThen(drone.getMission().uploadMission(missionItems))
                .andThen(drone.getMission().startMission())
                .subscribe()
        drone.action.arm().andThen(drone.action.takeoff()).subscribe()
    }

    private fun addMissionItems() {
        missionItems.add(generateMissionItem(47.398039859999997, 8.5455725400000002))
        missionItems.add(generateMissionItem(47.398036222362471, 8.5450146439425509))
        missionItems.add(generateMissionItem(47.397825620791885, 8.5450092830163271))
        missionItems.add(generateMissionItem(47.397832880000003, 8.5455939999999995))
    }

    private fun generateMissionItem(latitudeDeg: Double, longitudeDeg: Double): Mission.MissionItem {
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
