package ch.epfl.sdp.drone

import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer

object DroneInstanceProvider {
    private const val USE_REMOTE_BACKEND = false // False for running MavsdkServer locally, True to connect to a remote instance
    private const val REMOTE_BACKEND_IP_ADDRESS = "10.0.2.2" //IP of the remote instance
    private const val REMOTE_BACKEND_PORT = 50051 // Port of the remote instance

    var provide = {
        if (USE_REMOTE_BACKEND) {
            System(REMOTE_BACKEND_IP_ADDRESS, REMOTE_BACKEND_PORT)
        } else {
            // Works for armeabi-v7a and arm64-v8a (not x86 or x86_64)
            val mavsdkServer = MavsdkServer()
            val mavsdkServerPort = mavsdkServer.run()
            System("localhost", mavsdkServerPort)
        }
    }
}