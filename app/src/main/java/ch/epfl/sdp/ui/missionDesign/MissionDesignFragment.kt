package ch.epfl.sdp.ui.missionDesign

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ch.epfl.sdp.CentralLocationListener
import ch.epfl.sdp.LocationSubscriber
import ch.epfl.sdp.R

class MissionDesignFragment : Fragment(), LocationSubscriber {

    private lateinit var missionDeignViewModel: MissionDeignViewModel

    var location = Location(LocationManager.GPS_PROVIDER)
    var longitudeValueGPS: TextView? = null
    var latitudeValueGPS:TextView? = null
    private val requestCode = 1011

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        missionDeignViewModel =
                ViewModelProviders.of(this).get(MissionDeignViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_mission_design, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        missionDeignViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Misson design fragment \n (select areas here)"
        })

        //setContentView(R.layout.activity_g_p_s)

        location.latitude = Double.NaN
        location.longitude = Double.NaN
        updateLocationInUI(location)

        return root
    }

    override fun onStart() {
        super.onStart()
        longitudeValueGPS = view!!.findViewById<TextView>(R.id.longitudeValueGPS)
        latitudeValueGPS = view!!.findViewById<TextView>(R.id.latitudeValueGPS)
    }

    override fun onResume() {
        super.onResume()
        CentralLocationListener.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        CentralLocationListener.unsubscribe(this)
    }

    /**
     * update the values in the UI
     */
    private fun updateLocationInUI(location: Location){
        activity!!.runOnUiThread{
            longitudeValueGPS?.text = location.longitude.toString()
            latitudeValueGPS?.text = location.latitude.toString()
        }
    }

    override fun onLocationChanged(location: Location) {
        updateLocationInUI(location)
    }
}
