package ch.epfl.sdp.drone

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.ui.toast.ToastHandler
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.mavsdk.mission.Mission
import io.mavsdk.telemetry.Telemetry
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


object Drone {
    // Maximum distance between passes in the strategy
    const val GROUND_SENSOR_SCOPE: Double = 15.0
    const val DEFAULT_ALTITUDE: Float = 10.0F
    const val MAX_DISTANCE_BETWEEN_POINTS_IN_AREA = 1000 //meters

    private const val WAIT_TIME_S: Long = 1

    val onMeasureTakenCallbacks = mutableListOf<(LatLng, Double) -> Unit>()

    private val disposables: MutableList<Disposable> = ArrayList()
    val positionLiveData: MutableLiveData<LatLng> = MutableLiveData()
    val batteryLevelLiveData: MutableLiveData<Float> = MutableLiveData()
    val absoluteAltitudeLiveData: MutableLiveData<Float> = MutableLiveData()
    val speedLiveData: MutableLiveData<Float> = MutableLiveData()
    val missionLiveData: MutableLiveData<List<Mission.MissionItem>> = MutableLiveData()
    val homeLocationLiveData: MutableLiveData<Telemetry.Position> = MutableLiveData()
    val isFlyingLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val isConnectedLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val isMissionPausedLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    /*Will be useful later on*/
    val debugGetSignalStrength: () -> Double = {
        positionLiveData.value?.distanceTo(LatLng(47.3975, 8.5445)) ?: 0.0
    }

    private val instance: System = DroneInstanceProvider.provide()
    var getSignalStrength: () -> Double = debugGetSignalStrength


    init {
        disposables.add(instance.telemetry.flightMode.distinctUntilChanged()
                .subscribe(
                        { flightMode ->
                            if (flightMode == Telemetry.FlightMode.HOLD) isMissionPausedLiveData.postValue(true)
                            if (flightMode == Telemetry.FlightMode.MISSION) isMissionPausedLiveData.postValue(false)
                        },
                        { error -> Timber.e("Error Flight Mode: $error") }
                )
        )
        disposables.add(instance.telemetry.armed.distinctUntilChanged()
                .subscribe(
                        { armed -> if (!armed) isMissionPausedLiveData.postValue(true) },
                        { error -> Timber.e("Error Armed : $error") }
                )
        )
        disposables.add(instance.telemetry.position.distinctUntilChanged()
                .subscribe(
                        { position ->
                            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
                            positionLiveData.postValue(latLng)
                            //absoulte Atlitude is the altitude w.r. to the sea level
                            absoluteAltitudeLiveData.postValue(position.absoluteAltitudeM)
                            //Relative Altitude is the altitude w.r. to the take off level
                        },
                        { error -> Timber.e("Error Telemetry Position : $error") }
                )
        )
        disposables.add(instance.telemetry.battery.distinctUntilChanged()
                .subscribe(
                        { battery -> batteryLevelLiveData.postValue(battery.remainingPercent) },
                        { error -> Timber.e("Error Battery : $error") }
                )
        )
        disposables.add(instance.telemetry.positionVelocityNed.distinctUntilChanged()
                .subscribe(
                        { vector_speed -> speedLiveData.postValue(sqrt(vector_speed.velocity.eastMS.pow(2) + vector_speed.velocity.northMS.pow(2))) },
                        { error -> Timber.e("Error GroundSpeedNed : $error") }
                )
        )
        disposables.add(instance.telemetry.inAir.distinctUntilChanged()
                .subscribe(
                        { isFlying -> isFlyingLiveData.postValue(isFlying) },
                        { error -> Timber.e("Error inAir : $error") }
                )
        )
        disposables.add(instance.telemetry.home.distinctUntilChanged()
                .subscribe(
                        { home -> homeLocationLiveData.postValue(home) },
                        { error -> Timber.e("Error home : $error") }
                )
        )
        disposables.add(instance.core.connectionState.distinctUntilChanged()
                .subscribe(
                        { state -> isConnectedLiveData.postValue(state.isConnected) },
                        { error -> Timber.e("Error connectionState : $error") }
                )
        )
    }

    /**
     * @param missionPlan : the MissionPlan the drone will follow
     */
    fun startMission(missionPlan: Mission.MissionPlan) {
        disposables.add(
                getConnectedInstance()
                        .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                        .andThen(instance.mission.uploadMission(missionPlan))
                        .andThen(instance.action.arm())
                        .andThen(instance.mission.startMission())
                        .subscribe(
                                {
                                    this.missionLiveData.value = missionPlan.missionItems
                                    this.isMissionPausedLiveData.postValue(false)
                                    ToastHandler().showToast(R.string.drone_mission_success, Toast.LENGTH_SHORT)
                                },
                                {
                                    ToastHandler().showToast(R.string.drone_mission_error, Toast.LENGTH_SHORT)
                                }
                        )
        )
        this.missionLiveData.value = missionPlan.missionItems
        val isConnectedCompletable = instance.core.connectionState
                .filter { state -> state.isConnected }
                .firstOrError()
                .toCompletable()

        disposables.add(isConnectedCompletable
                .andThen(instance.mission.setReturnToLaunchAfterMission(true))
                .andThen(instance.mission.uploadMission(missionPlan))
                .andThen(instance.action.arm())
                .andThen(instance.mission.startMission())
                .subscribe(
                        { ToastHandler().showToast(R.string.drone_mission_success, Toast.LENGTH_SHORT) },
                        { ToastHandler().showToast(R.string.drone_mission_error, Toast.LENGTH_SHORT) })
        )

        disposables.add(instance.mission.missionProgress.subscribe {
            val missionItem = missionLiveData.value?.get(it.current)!!
            val location = LatLng(missionItem.latitudeDeg, missionItem.longitudeDeg)
            val signal = getSignalStrength()
            onMeasureTaken(location, signal)
        })
        //TODO See what to do with disposables added
    }

    private fun onMeasureTaken(location: LatLng, signalStrength: Double) {
        onMeasureTakenCallbacks.forEach {
            it(location, signalStrength)
        }
    }

    /**
     * Pauses the current Mission
     */
    fun pauseMission() {
        disposables.add(getConnectedInstance()
                .andThen(instance.mission.pauseMission())
                .subscribe(
                        {
                            this.isMissionPausedLiveData.postValue(true)
                            ToastHandler().showToast(R.string.drone_pause_success, Toast.LENGTH_SHORT)
                        },
                        {
                            ToastHandler().showToast(R.string.drone_pause_error, Toast.LENGTH_SHORT)
                        }
                )
        )
    }

    /**
     * Resumes the current Mission
     */
    fun resumeMission() {
        disposables.add(getConnectedInstance()
                .andThen(instance.mission.startMission())
                .subscribe(
                        {
                            this.isMissionPausedLiveData.postValue(false)
                            ToastHandler().showToast(R.string.drone_mission_success, Toast.LENGTH_SHORT)
                        },
                        {
                            ToastHandler().showToast(R.string.drone_mission_error, Toast.LENGTH_SHORT)
                        }
                )
        )
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
        disposables.add(
                getConnectedInstance()
                        .andThen(instance.mission.pauseMission())
                        .andThen(instance.mission.clearMission())
                        .andThen(instance.action.returnToLaunch())
                        .subscribe(
                                {
                                    this.missionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
                                    ToastHandler().showToast(R.string.drone_home_success, Toast.LENGTH_SHORT)
                                },
                                {
                                    ToastHandler().showToast(R.string.drone_home_error, Toast.LENGTH_SHORT)
                                }
                        )
        )
    }

    /**
     * @throws IllegalStateException
     * if user position is not available
     */
    fun returnToUserLocationAndLand() {
        val returnLocation = CentralLocationManager.currentUserPosition.value
                ?: throw IllegalStateException(MainApplication.applicationContext().getString(R.string.drone_user_error))
        disposables.add(
                getConnectedInstance()
                        .andThen(instance.mission.pauseMission())
                        .andThen(instance.mission.clearMission())
                        .andThen(instance.action.gotoLocation(returnLocation.latitude, returnLocation.longitude, 20.0F, 0F))
                        .subscribe(
                                {
                                    this.missionLiveData.value = listOf(DroneUtils.generateMissionItem(returnLocation.latitude, returnLocation.longitude, returnLocation.altitude.toFloat()))
                                    ToastHandler().showToast(R.string.drone_user_success, Toast.LENGTH_SHORT)
                                },
                                {
                                    ToastHandler().showToast(R.string.drone_user_error, Toast.LENGTH_SHORT)
                                }
                        )
        )

        disposables.add(
                instance.telemetry.position.subscribe(
                        { pos ->
                            val isRightPos = LatLng(pos.latitudeDeg, pos.longitudeDeg).distanceTo(returnLocation).roundToInt() == 0
                            val isStopped = speedLiveData.value?.roundToInt() == 0
                            if (isRightPos.and(isStopped)) instance.action.land().blockingAwait(WAIT_TIME_S, TimeUnit.SECONDS)
                            this.isMissionPausedLiveData.postValue(true)
                        },
                        { e -> Timber.e("ERROR LANDING : $e") }
                )
        )
    }
}