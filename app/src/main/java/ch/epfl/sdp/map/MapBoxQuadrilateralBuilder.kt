package ch.epfl.sdp.map

import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng

class MapBoxQuadrilateralBuilder(private val searchArea: QuadrilateralArea) : MapBoxEventManager {

    override fun onMapClicked(position: LatLng) {
        if (!searchArea.isComplete()) searchArea.addAngle(position)
    }

    override fun onMapLongClicked(position: LatLng) {

    }
}