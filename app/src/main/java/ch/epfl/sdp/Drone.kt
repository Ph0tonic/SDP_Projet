package ch.epfl.sdp

import io.mavsdk.System

object Drone {
    private const val BACKEND_IP_ADDRESS = "192.168.1.24"
    private const val PORT = 50020
    val instance : System = System(BACKEND_IP_ADDRESS, PORT)
}