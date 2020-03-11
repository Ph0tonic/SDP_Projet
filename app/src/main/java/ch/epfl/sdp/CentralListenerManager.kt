package ch.epfl.sdp

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import java.util.Collections.list

object CentralListenerManager : LocationListener {
    private var subscribers: List<LocationSubscriber> = emptyList()
    private lateinit var location: Location

    fun subscribe(subscriber: LocationSubscriber){
        subscribers += subscriber
    }

    fun unsubscribe(subscriber: LocationSubscriber){
        subscribers -= subscriber
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
        subscribers.forEach {
            subscriber -> subscriber.onLocationChanged(location)
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}
}