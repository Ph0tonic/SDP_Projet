package ch.epfl.sdp

import io.mavsdk.System

object Drone {
    private val BACKEND_IP_ADDRESS = "10.0.2.2" //emulator default address
    private const val PORT = 50020
    val instance : System = System(BACKEND_IP_ADDRESS, PORT)
}