package ch.epfl.sdp.drone

import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.database.data_manager.HeatmapDataManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer
import io.mavsdk.mission.Mission
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.pow
import kotlin.math.sqrt

object Drone {
    private const val USE_REMOTE_BACKEND = false // False for running MavsdkServer locally, True to connect to a remote instance
    private const val REMOTE_BACKEND_IP_ADDRESS = "10.0.2.2" // IP of the remote instance
    private const val REMOTE_BACKEND_PORT = 50051 // Port of the remote instance

    // Maximum distance between passes in the strategy
    const val GROUND_SENSOR_SCOPE: Double = 15.0
    const val DEFAULT_ALTITUDE: Float = 20.0F
    const val MAX_DISTANCE_BETWEEN_POINTS_IN_AREA = 1000 //meters

    private const val WAIT_TIME: Long = 200

    private val heatmapDataManager = HeatmapDataManager()

    private val disposables: MutableList<Disposable> = ArrayList()
    val currentPositionLiveData: MutableLiveData<LatLng> = MutableLiveData()
    val currentBatteryLevelLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentAbsoluteAltitudeLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentSpeedLiveData: MutableLiveData<Float> = MutableLiveData()
    val currentMissionLiveData: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()

    private val onMeasureTakenCallbacks = mutableListOf<(LatLng, Double) -> Unit>()

    /*Will be useful later on*/
    val debugGetSignalStrength: () -> Double = {
        currentPositionLiveData.value!!.distanceTo(LatLng(47.3975, 8.5445))
    }

    var getSignalStrength: () -> Double = debugGetSignalStrength

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
    }

    fun startMission(missionPlan: Mission.MissionPlan, groupId: String) {
        Timber.w("startMission Drone")
        this.currentMissionLiveData.value = missionPlan.missionItems
        val isConnectedCompletable = instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError()
                .toCompletable()

        val missionCallBack = { location: LatLng, signalStrength: Double ->
            heatmapDataManager.addMeasureToHeatmap(groupId, location, signalStrength)
        }

        isConnectedCompletable
                .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                .andThen(instance.mission.uploadMission(missionPlan))
                .andThen(instance.action.arm())
                .andThen {
                    onMeasureTakenCallbacks.add(missionCallBack)
                    Timber.w("Add callback for measure !")
                }
                .andThen(instance.mission.startMission())
                .doOnTerminate {
                    onMeasureTakenCallbacks.remove(missionCallBack)
                    Timber.w("On terminate !")
                }
                .subscribe()

        disposables.add(instance.mission.missionProgress.subscribe {
            Timber.w("Mission progress  YEAH !")
            val missionItem = currentMissionLiveData.value?.get(it.current)!!
            val location = LatLng(missionItem.latitudeDeg, missionItem.longitudeDeg)
            val signal = getSignalStrength()
            onMeasureTaken(location, signal)
        })
    }

    private fun onMeasureTaken(location: LatLng, signalStrength: Double) {
        onMeasureTakenCallbacks.forEach {
            it(location, signalStrength)
        }
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

    fun isFlying(): Boolean {
        return try {
            !instance.mission.isMissionFinished.blockingGet()
        } catch (e: RuntimeException) {
            false
        }
    }
}