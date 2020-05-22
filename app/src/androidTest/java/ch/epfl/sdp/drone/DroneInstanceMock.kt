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
import org.mockito.Mockito.`when`

object DroneInstanceMock {
    val droneSystem: System = Mockito.mock(System::class.java)
    val droneTelemetry: Telemetry = Mockito.mock(Telemetry::class.java)
    val droneCore: Core = Mockito.mock(Core::class.java)
    val droneMission: Mission = Mockito.mock(Mission::class.java)
    val droneAction: Action = Mockito.mock(Action::class.java)

    init {
        DroneInstanceProvider.provide = {
            droneSystem
        }

        `when`(droneSystem.telemetry)
                .thenReturn(droneTelemetry)
        `when`(droneSystem.core)
                .thenReturn(droneCore)
        `when`(droneSystem.mission)
                .thenReturn(droneMission)
        `when`(droneSystem.action)
                .thenReturn(droneAction)
    }

    fun setupDefaultMocks() {
        resetMocks()

        // Telemetry Mocks
        `when`(droneTelemetry.flightMode)
                .thenReturn(Flowable.fromArray(
                        Telemetry.FlightMode.LAND,
                        Telemetry.FlightMode.MISSION,
                        Telemetry.FlightMode.HOLD
                ))
        `when`(droneTelemetry.armed)
                .thenReturn(Flowable.fromArray(
                        true
                ))
        `when`(droneTelemetry.position)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
                ))
        `when`(droneTelemetry.battery)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Battery(0.0f, 0.0f)
                ))
        `when`(droneTelemetry.positionVelocityNed)
                .thenReturn(Flowable.fromArray(
                        Telemetry.PositionVelocityNed(
                                Telemetry.PositionNed(0.0f, 0.0f, 0.0f),
                                Telemetry.VelocityNed(0.0f, 0.0f, 0.0f)
                        )
                ))
        `when`(droneTelemetry.home)
                .thenReturn(Flowable.fromArray(
                        Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
                ))
        `when`(droneTelemetry.inAir)
                .thenReturn(Flowable.fromArray(
                        true
                ))

        //Core mocks
        `when`(droneCore.connectionState)
                .thenReturn(Flowable.fromArray(
                        Core.ConnectionState(0L, true)
                ))

        //Mission mocks
        `when`(droneMission.pauseMission())
                .thenReturn(Completable.complete())
        `when`(droneMission.setReturnToLaunchAfterMission(ArgumentMatchers.anyBoolean()))
                .thenReturn(Completable.complete())
        `when`(droneMission.uploadMission(ArgumentMatchers.any()))
                .thenReturn(Completable.complete())
        `when`(droneMission.startMission())
                .thenReturn(Completable.complete())
        `when`(droneMission.clearMission())
                .thenReturn(Completable.complete())
        `when`(droneMission.missionProgress)
                .thenReturn(Flowable.empty())

        //Action mocks
        `when`(droneAction.arm())
                .thenReturn(Completable.complete())
        `when`(droneAction.gotoLocation(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat()))
                .thenReturn(Completable.complete())
        `when`(droneAction.returnToLaunch())
                .thenReturn(Completable.complete())
        `when`(droneAction.land())
                .thenReturn(Completable.complete())
    }

    fun resetMocks() {
        Mockito.reset(droneAction, droneCore, droneMission, droneTelemetry)
    }
}