package ch.epfl.sdp.searcharea

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.ui.maps.IntersectionTools
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class QuadrilateralArea : SearchArea {

    private val angles: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())

    fun addAngle(angle: LatLng) {
        require(angles.value?.size!! < 4) { "Max number of angles reached" }
        angles.value?.add(angle)
        orderAngles()
        angles.notifyObserver()
    }

    fun moveAngle(old: LatLng, new: LatLng) {
        Log.e("MOVE ANGLE OLD ONE :", angles.value?.indexOf(old).toString())
        val oldIndex = angles.value?.withIndex()?.minBy { it.value.distanceTo(old) }?.index
        angles.value?.removeAt(oldIndex!!)
        angles.value?.add(new)
        orderAngles()
        angles.notifyObserver()
    }

    private fun orderAngles() {
        Log.e("ORDER ANGLES", "Call")
        Log.e("CONTENT", angles.value.toString())
        if (angles.value != null && angles.value?.size == 4) {
            Log.e("ORDER ANGLES", "ENTER")
            val data = angles.value!!

            // Diagonals should intersect
            if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                Log.e("ORDER ANGLES", "REORDER 1")
                Collections.swap(data, 1, 2)
                if (!IntersectionTools.doIntersect(data[0], data[2], data[1], data[3])) {
                    Log.e("ORDER ANGLES", "REORDER 2")
                    Collections.swap(data, 1, 2)
                    Collections.swap(data, 2, 3)
                }
            }
        }
    }

    override fun isComplete(): Boolean {
        return angles.value?.size == 4
    }

    override fun getLatLng(): MutableLiveData<MutableList<LatLng>> {
        return angles
    }
}