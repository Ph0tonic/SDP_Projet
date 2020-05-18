package ch.epfl.sdp.ui.drone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ch.epfl.sdp.R
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.utils.CentralLocationManager
import com.getbase.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry


/**
 * A simple [Fragment] subclass.
 * Use the [DroneStatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DroneStatusFragment : Fragment() {

    companion object {
        private const val DISTANCE_FORMAT = " %.1f m"
        private const val PERCENTAGE_FORMAT = " %.0f%%"
        private const val SPEED_FORMAT = " %.1f m/s"
    }

    private lateinit var droneBatteryLevelImageView: ImageView
    private lateinit var droneBatteryLevelTextView: TextView
    private lateinit var distanceToUserTextView: TextView
    private lateinit var droneAltitudeTextView: TextView
    private lateinit var droneSpeedTextView: TextView
    private lateinit var distanceToHomeTextView: TextView

    private val droneBatteryLevelDrawables = listOf(
            Pair(.0, R.drawable.ic_battery1),
            Pair(.05, R.drawable.ic_battery2),
            Pair(.23, R.drawable.ic_battery3),
            Pair(.41, R.drawable.ic_battery4),
            Pair(.59, R.drawable.ic_battery5),
            Pair(.77, R.drawable.ic_battery6),
            Pair(.95, R.drawable.ic_battery7)
    )

    private var droneAltitudeObserver = Observer<Float> { newAltitude: Float? ->
        updateTextView(droneAltitudeTextView, newAltitude?.toDouble(), DISTANCE_FORMAT)
    }
    private var droneSpeedObserver = Observer<Float> { newSpeed: Float? ->
        updateTextView(droneSpeedTextView, newSpeed?.toDouble(), SPEED_FORMAT)
    }
    private var dronePositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let {
            updateDroneUserDistance(it)
            updateDroneHomeDistance(it)}
    }
    private var userPositionObserver = Observer<LatLng> { newLatLng: LatLng? ->
        newLatLng?.let { updateUserDroneDistance(it) }
    }
    private var homePositionObserver = Observer<Telemetry.Position> {
        newPosition: Telemetry.Position? ->
        newPosition?.let {
            updateHomeDroneDistance(LatLng(it.latitudeDeg, it.longitudeDeg))
        }
    }
    private var droneBatteryObserver = Observer<Float> { newBatteryLevel: Float? ->
        // Always update the text string
        updateTextView(droneBatteryLevelTextView, newBatteryLevel?.times(100)?.toDouble(), PERCENTAGE_FORMAT)

        // Only update the icon if the battery level is not null
        newBatteryLevel?.let {
            val newBatteryDrawable = droneBatteryLevelDrawables
                    .filter { x -> x.first <= newBatteryLevel.coerceAtLeast(0f) }
                    .maxBy { x -> x.first }!!
                    .second
            droneBatteryLevelImageView.setImageResource(newBatteryDrawable)
            droneBatteryLevelImageView.tag = newBatteryDrawable
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_drone_status, container, false)

        droneBatteryLevelImageView = root.findViewById(R.id.battery_level_icon)
        droneBatteryLevelTextView = root.findViewById(R.id.battery_level)
        droneAltitudeTextView = root.findViewById(R.id.altitude)
        distanceToUserTextView = root.findViewById(R.id.distance_to_user)
        droneSpeedTextView = root.findViewById(R.id.speed)
        distanceToHomeTextView = root.findViewById(R.id.distance_to_home)
        return root
    }

    override fun onResume() {
        super.onResume()
        CentralLocationManager.currentUserPosition.observe(this, userPositionObserver)
        Drone.currentPositionLiveData.observe(this, dronePositionObserver)
        Drone.currentBatteryLevelLiveData.observe(this, droneBatteryObserver)
        Drone.currentAbsoluteAltitudeLiveData.observe(this, droneAltitudeObserver)
        Drone.currentSpeedLiveData.observe(this, droneSpeedObserver)
        Drone.currentHomeLiveData.observe(this, homePositionObserver)
    }

    override fun onPause() {
        super.onPause()
        CentralLocationManager.currentUserPosition.removeObserver(userPositionObserver)
        Drone.currentPositionLiveData.removeObserver(dronePositionObserver)
        Drone.currentBatteryLevelLiveData.removeObserver(droneSpeedObserver)
        Drone.currentAbsoluteAltitudeLiveData.removeObserver(droneAltitudeObserver)
        Drone.currentSpeedLiveData.removeObserver(droneSpeedObserver)
        Drone.currentHomeLiveData.removeObserver(homePositionObserver)
    }

    /**
     * Updates the text of the given textView with the given value and format, or the default string
     * if the value is null
     */
    private fun updateTextView(textView: TextView, value: Double?, formatString: String) {
        textView.text = value?.let { formatString.format(it) } ?: getString(R.string.no_info)
    }

    /**
     * Update Drone to User distance.
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateDroneUserDistance(newLatLng: LatLng) {
        CentralLocationManager.currentUserPosition.value.let {
            updateTextView(distanceToUserTextView, it?.distanceTo(newLatLng), DISTANCE_FORMAT)
        }
    }

    /**
     * Update User to Drone distance
     */
    private fun updateUserDroneDistance(userLatLng: LatLng) {
        Drone.currentPositionLiveData.value?.let {
            updateTextView(distanceToUserTextView, it.distanceTo(userLatLng), DISTANCE_FORMAT)
        }
    }

    /**
     * Update Drone to Home distance.
     *
     * @param newLatLng new position of the vehicle
     */
    private fun updateDroneHomeDistance(newLatLng: LatLng) {
        Drone.currentHomeLiveData.value?.let {
            updateTextView(distanceToHomeTextView, LatLng(it.latitudeDeg, it.longitudeDeg).distanceTo(newLatLng), DISTANCE_FORMAT)
        }
    }

    /**
     * Update Home to Drone distance
     *
     * @param homeLatLng new home position
     */
    private fun updateHomeDroneDistance(homePosition: LatLng) {
        Drone.currentPositionLiveData.value?.let {
            updateTextView(distanceToHomeTextView, it.distanceTo(homePosition), DISTANCE_FORMAT)
        }
    }
}
