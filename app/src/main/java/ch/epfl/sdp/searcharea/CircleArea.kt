package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng

class CircleArea(val center: LatLng, val outer: LatLng) : SearchArea