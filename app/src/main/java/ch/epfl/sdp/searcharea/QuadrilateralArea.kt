package ch.epfl.sdp.searcharea

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.ui.maps.IntersectionTools
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class QuadrilateralArea : SearchArea() {

    fun addAngle(angle: LatLng) {
        require(latLngs.value?.size!! < 4) { "Max number of latLngs reached" }
        latLngs.value?.add(angle)
        orderlatLngs()
        latLngs.notifyObserver()
    }

    fun moveAngle(old: LatLng, new: LatLng) {
        val oldIndex = latLngs.value?.withIndex()?.minBy { it.value.distanceTo(old) }?.index
        latLngs.value?.removeAt(oldIndex!!)
        latLngs.value?.add(new)
        orderlatLngs()
        latLngs.notifyObserver()
    }

    private fun orderlatLngs() {
        if (latLngs.value != null && latLngs.value?.size == 4) {
            val data = latLngs.value!!

            // Diagonals should intersect
            if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                Collections.swap(data, 1, 2)
                if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                    Collections.swap(data, 1, 2)
                    Collections.swap(data, 2, 3)
                }
            }
        }
    }

    override fun isComplete(): Boolean {
        return latLngs.value?.size == 4
    }

    override fun reset() {
        latLngs.value?.clear()
        latLngs.notifyObserver()
    }
}