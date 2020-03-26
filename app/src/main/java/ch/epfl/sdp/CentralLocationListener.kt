package ch.epfl.sdp

import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi

object CentralLocationListener : LocationListener {
    private val subscribers: MutableSet<LocationSubscriber> = mutableSetOf()
    private lateinit var location: Location

    fun subscribe(subscriber: LocationSubscriber){
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: LocationSubscriber){
        subscribers.remove(subscriber)
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
        subscribers.forEach {
            subscriber ->
            run {
                subscriber.onLocationChanged(location)
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onProviderDisabled(s: String) {
        CentralLocationManager.checkLocationSetting()
    }
}