package ch.epfl.sdp.drone

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.System
import io.reactivex.disposables.Disposable

object Drone {
    private const val BACKEND_IP_ADDRESS = "192.168.1.24"
    private const val PORT = 50020

    private val disposables: MutableList<Disposable> = ArrayList()
    val currentPositionLiveData = MutableLiveData<LatLng>()

    val instance : System

    init {
        instance = System(BACKEND_IP_ADDRESS, PORT)
        disposables.add(instance.telemetry.flightMode.distinct()
                .subscribe { flightMode -> Log.d("DRONE","flight mode: $flightMode") })
        disposables.add(instance.telemetry.armed.distinct()
                .subscribe { armed -> Log.d("DRONE","armed: $armed") })
        disposables.add(instance.telemetry.position.subscribe { position ->
            val latLng = LatLng(position.latitudeDeg, position.longitudeDeg)
            currentPositionLiveData.postValue(latLng)
        })
    }
}