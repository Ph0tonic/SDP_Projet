package ch.epfl.sdp.drone

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Drone {
    private const val USE_REMOTE_BACKEND = true // False for running MavsdkServer locally, True to connect to a remote instance
    private const val REMOTE_BACKEND_IP_ADDRESS = "10.0.2.2" // IP of the remote instance
    private const val REMOTE_BACKEND_PORT = 50051 // Port of the remote instance

    // Maximum distance between passes in the strategy
    const val GROUND_SENSOR_SCOPE: Double = 15.0
    const val DEFAULT_ALTITUDE: Float = 20.0F

    private const val WAIT_TIME: Long = 200

    private val disposables: MutableList<Disposable> = ArrayList()
    val currentPositionLiveData: MutableLiveData<LatLng> = MutableLiveData()
    val currentBatteryLevelLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentAbsoluteAltitudeLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentSpeedLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentMissionLiveData: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()
    val currentFlyingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val currentHomeLiveData: MutableLiveData<Telemetry.Position> = MutableLiveData()

    lateinit var getSignalStrength: () -> Double

    /*Will be useful later on*/
    val debugGetSignalStrength: () -> Double = {
        currentPositionLiveData.value?.distanceTo(LatLng(47.3975, 8.5445)) ?: 0.0
    }

    private val instance: System

    init {
        instance = if (USE_REMOTE_BACKEND) {
            System(REMOTE_BACKEND_IP_ADDRESS, REMOTE_BACKEND_PORT)
        } else {
            // Works for armeabi-v7a and arm64-v8a (not x86 or x86_64)
            val mavsdkServer = MavsdkServer()
            val mavsdkServerPort = mavsdkServer.run()
            System("localhost", mavsdkServerPort)
        }

        disposables.add(instance.telemetry.flightMode.distinct()
                .subscribe(
                        { flightMode -> Timber.d("flight mode: $flightMode") },
                        { error -> Timber.e("Error Flight Mode: $error") }
                ))
        disposables.add(instance.telemetry.armed.distinct()
                .subscribe(
                        { armed -> Timber.d("armed: $armed") },
                        { error -> Timber.e("Error Armed : $error") }
                ))
        disposables.add(instance.telemetry.position
                .subscribe(
                        { position ->
                            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
                            currentPositionLiveData.postValue(latLng)
                            //absoulte Atlitude is the altitude w.r. to the sea level
                            currentAbsoluteAltitudeLiveData.postValue(position.absoluteAltitudeM)
                            //Relative Altitude is the altitude w.r. to the take off level
                        },
                        { error -> Timber.e("Error Telemetry Position : $error") }
                ))
        disposables.add(instance.telemetry.battery
                .subscribe(
                        { battery -> currentBatteryLevelLiveData.postValue(battery.remainingPercent) },
                        { error -> Timber.e("Error Battery : $error") }
                ))
        disposables.add(instance.telemetry.positionVelocityNed
                .subscribe(
                        { vector_speed -> currentSpeedLiveData.postValue(sqrt(vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2))) },
                        { error -> Timber.e("Error GroundSpeedNed : $error") }))
        disposables.add(instance.telemetry.inAir
                .subscribe(
                        { isFlying -> currentFlyingLiveData.postValue(isFlying) },
                        { error -> Timber.e("Error inAir : $error") }
                ))
        disposables.add(instance.telemetry.home
                .subscribe(
                        { home -> currentHomeLiveData.postValue(home) },
                        { error -> Timber.e("Error home : $error") }
                ))
    }

    fun startMission(missionPlan: Mission.MissionPlan) {
        this.currentMissionLiveData.value = missionPlan.missionItems
        val isConnectedCompletable = instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError()
                .toCompletable()

        disposables.add(
                isConnectedCompletable
                        .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                        .andThen(instance.mission.uploadMission(missionPlan))
                        .andThen(instance.action.arm())
                        .andThen(instance.mission.startMission())
                        .subscribe(
                                { Timber.d("Mission started successfully") },
                                { error -> Timber.e("Error : %s", error.message) }))
    }

    fun isConnected(): Boolean {
        return try {
            instance.core.connectionState
                    .filter { state -> state.isConnected }
                    .firstOrError()
                    .toFuture()
                    .get(WAIT_TIME, TimeUnit.MILLISECONDS).isConnected
        } catch (e: TimeoutException) {
            false
        }
    }

    private fun getConnectionState(): Completable {
        return instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError().toCompletable()
    }

    /**
     * Throws an IllegalStateException is the home position is not available
     */
    fun returnHome() {
        val returnLocation = currentHomeLiveData.value?.let { LatLng(it.latitudeDeg, it.longitudeDeg) }
        if (returnLocation != null) {
            this.currentMissionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
            disposables.add(
                    getConnectionState()
                            .andThen(instance.mission.pauseMission())
                            .andThen(instance.mission.clearMission())
                            .andThen(instance.action.returnToLaunch())
                            .subscribe())
        } else {
            throw IllegalStateException(MainApplication.applicationContext().getString(R.string.drone_home_error))
        }
    }

    /**
     * If the user position is not available, the drone will go to his home location by default
     */
    fun returnUser() {
        val returnLocation = CentralLocationManager.currentUserPosition.value
                ?: currentHomeLiveData.value?.let { LatLng(it.latitudeDeg, it.longitudeDeg) }
        if (returnLocation != null) {
            this.currentMissionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
            getConnectionState()
                    .andThen(instance.mission.pauseMission())
                    .andThen(instance.mission.clearMission())
                    .andThen(instance.action.gotoLocation(returnLocation.latitude, returnLocation.longitude, 20.0F, 0F))
                    .subscribe()

            disposables.add(
                    instance.telemetry.position.subscribe(
                            { pos ->
                                val isRightPos = LatLng(pos.latitudeDeg, pos.longitudeDeg).distanceTo(returnLocation).roundToInt() == 0
                                val isStopped = currentSpeedLiveData.value?.roundToInt() == 0
                                if (isRightPos.and(isStopped)) instance.action.land().blockingAwait()
                            },
                            { e -> Timber.e("ERROR LANDING : $e") }))
        } else {
            throw IllegalStateException(MainApplication.applicationContext().getString(R.string.drone_home_error))
        }
    }
}