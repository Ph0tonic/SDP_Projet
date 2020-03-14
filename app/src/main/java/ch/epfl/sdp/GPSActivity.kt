package ch.epfl.sdp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.Double.Companion.NaN


class GPSActivity : AppCompatActivity(), LocationSubscriber {

    private var locationManager: LocationManager? = null
    var longitudeGPS = 0.0
    var latitudeGPS = 0.0
    var longitudeValueGPS: TextView? = null
    var latitudeValueGPS:TextView? = null
    private val requestCode = 1011


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_p_s)
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        longitudeValueGPS = findViewById<TextView>(R.id.longitudeValueGPS)
        latitudeValueGPS = findViewById<TextView>(R.id.latitudeValueGPS)

        longitudeGPS = NaN
        latitudeGPS = NaN
        runOnUiThread {
            longitudeValueGPS?.text = longitudeGPS.toString()
            latitudeValueGPS?.text = latitudeGPS.toString()
        }

        /*
        if(checkPermission()){
            locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener)
        }

         */
        CentralLocationManager.onCreate(this)

    }

    override fun onStart() {
        super.onStart()
        CentralLocationListener.subscribe(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        //checkPermission() && checkLocationSetting()
        CentralLocationManager.onResume()
    }

    fun goToMain(view: View?) {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
    /*
    private fun checkLocationSetting(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

     */
    /*
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        else{
            //requestPermissions()
            return false
        }
    }

     */
    /*
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions(){
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        Log.d("-----------------------","request permission")

    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("-----------------------","dawufhuiawhfuwh")
        if(requestCode == this.requestCode){
            val granted: Boolean = grantResults.all { i -> i == PackageManager.PERMISSION_GRANTED}
            if(grantResults.isNotEmpty() && granted){
                locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener);
            }
            else{
                longitudeGPS = NaN
                latitudeGPS = NaN
                runOnUiThread {
                    longitudeValueGPS?.text = longitudeGPS.toString()
                    latitudeValueGPS?.text = latitudeGPS.toString()
                    Toast.makeText(this@GPSActivity, "Please Enable Location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun isLocationEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }
    private fun showAlert() {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("This part of the app cannot function without location, please enable it")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }

     */

    override fun onLocationChanged(location: Location) {
        longitudeGPS = location.longitude
        latitudeGPS = location.latitude
        runOnUiThread {
            longitudeValueGPS?.text = longitudeGPS.toString()
            latitudeValueGPS?.text = latitudeGPS.toString()
            //Toast.makeText(this@GPSActivity, "GPS Provider update", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        CentralLocationListener.unsubscribe(this)
        CentralLocationManager.onStop()
    }

}
