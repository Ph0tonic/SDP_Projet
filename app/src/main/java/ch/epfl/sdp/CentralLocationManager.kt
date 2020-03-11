package ch.epfl.sdp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.tasks.OnFailureListener

object CentralLocationManager {
    var isLive: Boolean = false
    private var locationManager: LocationManager? = null
    private lateinit var activity: Activity
    private const val requestCode = 1011


    @RequiresApi(Build.VERSION_CODES.M)
    fun onCreate(activity: Activity) {
        this.activity = activity
        locationManager = this.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if(checkPermission()){
            locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onResume(){
        checkPermission() && checkLocationSetting()
    }

    fun onStop(){

    }

    private fun checkLocationSetting(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true
        }
        else{
            requestPermissions()
            return false
        }
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions(){
        activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == this.requestCode){
            val granted: Boolean = grantResults.all { i -> i == PackageManager.PERMISSION_GRANTED}
            if(grantResults.isNotEmpty() && granted){
                if (activity.checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // There's no logical way to reach here
                    return
                }
                locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener);
            }
            else{
                //onFailure
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }
}