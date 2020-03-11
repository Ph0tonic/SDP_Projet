package ch.epfl.sdp

import android.location.Location
import android.location.LocationListener

interface LocationSubscriber {
    //val locationListener: LocationListener
    fun onLocationChanged(location: Location)
}