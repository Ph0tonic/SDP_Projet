package ch.epfl.sdp

import android.location.Location
import android.location.LocationListener

interface LocationSubscriber {
    fun onLocationChanged(location: Location)
}