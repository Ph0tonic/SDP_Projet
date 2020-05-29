package ch.epfl.sdp.drone

import io.mavsdk.System
import io.mavsdk.action.Action
import io.mavsdk.core.Core
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.Flowable
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

object DroneInstanceMock {
    val droneSystem = Mockito.mock(System::class.java)
    val droneTelemetry = Mockito.mock(Telemetry::class.java)
    val droneCore = Mockito.mock(Core::class.java)
    val droneMission = Mockito.mock(Mission::class.java)
    val droneAction = Mockito.mock(Action::class.java)

    init {
        DroneInstanceProvider.provide = {
            droneSystem
        }

        Mockito.`when`(droneSystem.telemetry)
                .thenReturn(droneTelemetry)
        Mockito.`when`(droneSystem.core)
                .thenReturn(droneCore)
        Mockito.`when`(droneSystem.mission)
                .thenReturn(droneMission)
        Mockito.`when`(droneSystem.action)
                .thenReturn(droneAction)
    }


    fun setupDefaultMocks() {
        resetMocks()
        // Telemetry Mocks
        Mockito.`when`(droneTelemetry.flightMode)
                .thenReturn(Flowable.fromArray(
                        Telemetry.FlightMode.LAND,
                        Telemetry.FlightMode.MISSION,
                        Telemetry.FlightMode.HOLD
                ))
        Mockito.`when`(droneTelemetry.armed)
                .thenReturn(Flowable.fromArray(
                        true
                ))
        Mockito.`when`(droneTelemetry.position)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
                ))
        Mockito.`when`(droneTelemetry.battery)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Battery(0.0f, 0.0f)
                ))
        Mockito.`when`(droneTelemetry.positionVelocityNed)
                .thenReturn(Flowable.fromArray(
                        Telemetry.PositionVelocityNed(
                                Telemetry.PositionNed(0.0f, 0.0f, 0.0f),
                                Telemetry.VelocityNed(0.0f, 0.0f, 0.0f)
                        )
                ))
        Mockito.`when`(droneTelemetry.home)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
                ))
        Mockito.`when`(droneTelemetry.inAir)
                .thenReturn(Flowable.fromArray(
                        true
                ))

        //Core mocks
        Mockito.`when`(droneCore.connectionState)
                .thenReturn(Flowable.fromArray(
                        Core.ConnectionState(0L, true)
                ))

        //Mission mocks
        Mockito.`when`(droneMission.pauseMission())
                .thenReturn(Completable.complete())
        Mockito.`when`(droneMission.setReturnToLaunchAfterMission(ArgumentMatchers.anyBoolean()))
                .thenReturn(Completable.complete())
        Mockito.`when`(droneMission.uploadMission(ArgumentMatchers.any()))
                .thenReturn(Completable.complete())
        Mockito.`when`(droneMission.startMission())
                .thenReturn(Completable.complete())
        Mockito.`when`(droneMission.clearMission())
                .thenReturn(Completable.complete())

        //Action mocks
        Mockito.`when`(droneAction.arm())
                .thenReturn(Completable.complete())
        Mockito.`when`(droneAction.gotoLocation(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat()))
                .thenReturn(Completable.complete())
        Mockito.`when`(droneAction.returnToLaunch())
                .thenReturn(Completable.complete())
        Mockito.`when`(droneAction.land())
                .thenReturn(Completable.complete())
    }

    fun resetMocks() {
        Mockito.reset(droneAction, droneCore, droneMission, droneTelemetry)
    }
}