package ch.epfl.sdp.utils

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
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import ch.epfl.sdp.MainApplication
import com.mapbox.mapboxsdk.geometry.LatLng

object CentralLocationManager {

    private const val requestCode = 1011
    private val requiredPermissions = setOf(ACCESS_FINE_LOCATION)

    private lateinit var locationManager: LocationManager
    private var activity: Activity? = null
    internal var currentUserPosition: MutableLiveData<LatLng> = MutableLiveData<LatLng>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun configure(activity: Activity, context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.activity = activity

        if (checkAndRequestPermission(context)) {
            locationManager.requestLocationUpdates(
                    GPS_PROVIDER, 500, 10f, CentralLocationListener)
        }
    }

    fun configure(activity: Activity) {
        this.configure(activity, MainApplication.applicationContext())
    }

    fun onDestroy() {
        activity = null
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
                    activity!!.startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { _, _ -> }

        locationDisabledAlert.show()
    }

    private fun checkAndRequestPermission(context: Context): Boolean {
        val hasPermission = checkPermission(context)
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity!!, requiredPermissions.toTypedArray(), requestCode)
        }
        return hasPermission
    }

    private fun checkPermission(context: Context): Boolean {
        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CentralLocationManager.requestCode && checkPermission(MainApplication.applicationContext())) {
            locationManager.requestLocationUpdates(
                    GPS_PROVIDER, 500, 10f, CentralLocationListener)
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(GPS_PROVIDER)
    }

    private object CentralLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentUserPosition.postValue(LatLng(location))
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onProviderDisabled(s: String) {
            checkLocationSetting()
        }
    }
}


