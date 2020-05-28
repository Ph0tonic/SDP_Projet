package ch.epfl.sdp.drone

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.ui.toast.ToastHandler
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
    private const val REMOTE_BACKEND_IP_ADDRESS = "10.0.2.2" //IP of the remote instance
    private const val REMOTE_BACKEND_PORT = 50051 // Port of the remote instance

    // Maximum distance between passes in the strategy
    const val GROUND_SENSOR_SCOPE: Double = 15.0
    const val DEFAULT_ALTITUDE: Float = 10.0F
    const val MAX_DISTANCE_BETWEEN_POINTS_IN_AREA = 1000 //meters

    private const val WAIT_TIME: Long = 200

    private val disposables: MutableList<Disposable> = ArrayList()
    val positionLiveData: MutableLiveData<LatLng> = MutableLiveData()
    val batteryLevelLiveData: MutableLiveData<Float> = MutableLiveData()
    val absoluteAltitudeLiveData: MutableLiveData<Float> = MutableLiveData()
    val speedLiveData: MutableLiveData<Float> = MutableLiveData()
    val missionLiveData: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()
    val isFlyingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val homeLocationLiveData: MutableLiveData<Telemetry.Position> = MutableLiveData()
    val isConnectedLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    lateinit var getSignalStrength: () -> Double

    /*Will be useful later on*/
    val debugGetSignalStrength: () -> Double = {
        positionLiveData.value?.distanceTo(LatLng(47.3975, 8.5445)) ?: 0.0
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
                            positionLiveData.postValue(latLng)
                            //absoulte Atlitude is the altitude w.r. to the sea level
                            absoluteAltitudeLiveData.postValue(position.absoluteAltitudeM)
                            //Relative Altitude is the altitude w.r. to the take off level
                        },
                        { error -> Timber.e("Error Telemetry Position : $error") }
                ))
        disposables.add(instance.telemetry.battery
                .subscribe(
                        { battery -> batteryLevelLiveData.postValue(battery.remainingPercent) },
                        { error -> Timber.e("Error Battery : $error") }
                ))
        disposables.add(instance.telemetry.positionVelocityNed
                .subscribe(
                        { vector_speed -> speedLiveData.postValue(sqrt(vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2))) },
                        { error -> Timber.e("Error GroundSpeedNed : $error") }))
        disposables.add(instance.telemetry.inAir
                .subscribe(
                        { isFlying -> isFlyingLiveData.postValue(isFlying) },
                        { error -> Timber.e("Error inAir : $error") }
                ))
        disposables.add(instance.telemetry.home
                .subscribe(
                        { home -> homeLocationLiveData.postValue(home) },
                        { error -> Timber.e("Error home : $error") }
                ))
        disposables.add(instance.core.connectionState
                .subscribe(
                        {state -> isConnectedLiveData.postValue(state.isConnected)},
                        { error -> Timber.e("Error home : $error")}
                ))
    }

    fun startMission(missionPlan: Mission.MissionPlan) {
        this.missionLiveData.value = missionPlan.missionItems
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
                                { ToastHandler().showToast(R.string.drone_mission_success, Toast.LENGTH_SHORT) },
                                { ToastHandler().showToast(R.string.drone_mission_error, Toast.LENGTH_SHORT) }))
    }

    /**
     * @return the connected instance as a Completable
     */
    private fun getConnectedInstance(): Completable {
        return instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError().toCompletable()
    }

    /**
     * @throws IllegalStateException
     * if the home position is not available
     */
    fun returnToHomeLocationAndLand() {
        val returnLocation = homeLocationLiveData.value?.let { LatLng(it.latitudeDeg, it.longitudeDeg) }
                ?: throw IllegalStateException(MainApplication.applicationContext().getString(R.string.drone_home_error))
        this.missionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
        disposables.add(
                getConnectedInstance()
                        .andThen(instance.mission.pauseMission())
                        .andThen(instance.mission.clearMission())
                        .andThen(instance.action.returnToLaunch())
                        .subscribe(
                                { ToastHandler().showToast(R.string.drone_home_success, Toast.LENGTH_SHORT) },
                                { ToastHandler().showToast(R.string.drone_home_error, Toast.LENGTH_SHORT) }))
    }

    /**
     * @throws IllegalStateException
     * if user position is not available
     */
    fun returnToUserLocationAndLand() {
        val returnLocation = CentralLocationManager.currentUserPosition.value
                ?: throw IllegalStateException(MainApplication.applicationContext().getString(R.string.drone_user_error))
        this.missionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
        disposables.add(
                getConnectedInstance()
                        .andThen(instance.mission.pauseMission())
                        .andThen(instance.mission.clearMission())
                        .andThen(instance.action.gotoLocation(returnLocation.latitude, returnLocation.longitude, 20.0F, 0F))
                        .subscribe(
                                { ToastHandler().showToast(R.string.drone_user_success, Toast.LENGTH_SHORT) },
                                { ToastHandler().showToast(R.string.drone_user_error, Toast.LENGTH_SHORT) }))

        disposables.add(
                instance.telemetry.position.subscribe(
                        { pos ->
                            val isRightPos = LatLng(pos.latitudeDeg, pos.longitudeDeg).distanceTo(returnLocation).roundToInt() == 0
                            val isStopped = speedLiveData.value?.roundToInt() == 0
                            if (isRightPos.and(isStopped)) instance.action.land().blockingAwait()
                        },
                        { e -> Timber.e("ERROR LANDING : $e") }))
    }
}