package ch.epfl.sdp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng

object CentralLocationManager {

    private const val requestCode = 1011

    private lateinit var locationManager: LocationManager
    private lateinit var activity: Activity
    internal var currentUserPosition: MutableLiveData<LatLng> = MutableLiveData<LatLng>()

    fun configure(activity: Activity) {
        CentralLocationManager.activity = activity
        locationManager = CentralLocationManager.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (checkAndRequestPermission()) {
            locationManager.requestLocationUpdates(
                    GPS_PROVIDER, 500, 10f, CentralLocationListener)
        }
    }

    fun checkLocationSetting(): Boolean {
        if (!isLocationEnabled()) {
            showLocationDisabledAlert()
        }
        return isLocationEnabled()
    }

    private fun showLocationDisabledAlert() {
        val locationDisabledAlert: AlertDialog.Builder = AlertDialog.Builder(activity)
        locationDisabledAlert.setTitle("Enable Location")
                .setMessage("This part of the app cannot function without location, please enable it")
                .setPositiveButton("Location Settings") { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { _, _ -> }

        locationDisabledAlert.show()
    }

    private fun checkAndRequestPermission(): Boolean {
        val hasPermission = checkPermission()
        if (!hasPermission) {
            requestPermissions()
        }
        return hasPermission
    }

    private fun checkPermission(): Boolean {
        return checkPermission(ACCESS_FINE_LOCATION) && checkPermission(ACCESS_COARSE_LOCATION)
    }

    private fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), requestCode)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CentralLocationManager.requestCode && checkPermission()) {
            locationManager.requestLocationUpdates(
                    GPS_PROVIDER, 500, 10f, CentralLocationListener)
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(GPS_PROVIDER)
    }
}

private object CentralLocationListener : LocationListener {

    override fun onLocationChanged(location: Location) {
        CentralLocationManager.currentUserPosition.postValue(LatLng(location))
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onProviderDisabled(s: String) {
        CentralLocationManager.checkLocationSetting()
    }
}
