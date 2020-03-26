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
    private lateinit var locationManager: LocationManager
    private lateinit var activity: Activity
    private lateinit var locationDisabledAlert: AlertDialog.Builder
    private const val requestCode = 1011


    fun configure(activity: Activity) {
        this.activity = activity
        locationManager = this.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationDisabledAlert = AlertDialog.Builder(this.activity)
        locationDisabledAlert.setTitle("Enable Location")
                ?.setMessage("This part of the app cannot function without location, please enable it")
                ?.setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(myIntent)
                }
                ?.setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->})

        if(checkPermission()){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener)
        }
    }

    fun checkLocationSetting(): Boolean {
        if (!isLocationEnabled()){
            locationDisabledAlert.show()
        }
        return isLocationEnabled()
    }


    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity.applicationContext,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity!!.applicationContext,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            false
        } else {
            true
        }
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), requestCode)
    }

    private fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == this.requestCode && checkPermission()){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 1000, 10f, CentralLocationListener)
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }
}