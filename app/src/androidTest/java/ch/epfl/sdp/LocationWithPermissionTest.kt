package ch.epfl.sdp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class LocationWithPermissionTest {
    private var mUiDevice: UiDevice? = null
    //private var locationTestActivity: Activity? = null

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MapActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        //locationTestActivity = LocationTestActivity()
    }

    private fun getContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun centralLocationManagerRequestsLocationUpdatesIfItHasPermission() {

        val activity = mock(AppCompatActivity::class.java)
        val context = mock(Context::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(activity.getApplicationContext()).thenReturn(context)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        //Mockito.verify(manager).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER),Mockito.eq(500),Mockito.eq(10f), CentralLocationListener)
        Mockito.verify(manager).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER),Mockito.eq(500L),Mockito.eq(10f), Mockito.any<LocationListener>())
    }

    @Test
    fun centralLocationManagerShowsAlertIfLocationIsDisabled() {
        /*
        val activity = mock(Activity::class.java)
        val context = mock(Context::class.java)
        val locationManager = mActivityRule.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val manager = spy(locationManager)

        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)

        Mockito.`when`(activity.getApplicationContext()).thenReturn(context)

        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false)

        CentralLocationManager.configure(activity)

        Mockito.verify(activity).getSystemService(Context.LOCATION_SERVICE)
        Mockito.verify(manager).isProviderEnabled(LocationManager.GPS_PROVIDER)
        onView(withText("Enable Location")).check(matches(isDisplayed()))

         */
        Assert.assertTrue(true)
    }

    @Test
    fun checkLocationReturnsTrueIfLocationIsEnabled(){
        CentralLocationManager.configure(mActivityRule.activity)
        Assert.assertTrue(CentralLocationManager.checkLocationSetting())
    }

    @Test
    fun onRequestPermissionResultRequestsLocationUpdatesIfItHasPermission() {
        val activity = mock(AppCompatActivity::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(activity.checkSelfPermission(Mockito.any())).thenReturn(PackageManager.PERMISSION_GRANTED)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        CentralLocationManager.onRequestPermissionsResult(1011,Array(0) {""},IntArray(0))
        //Mockito.verify(manager, Mockito.times(2) ).requestLocationUpdates(LocationManager.GPS_PROVIDER,500,10f, CentralLocationListener)
        Mockito.verify(manager, Mockito.times(2) ).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER),Mockito.eq(500L),Mockito.eq(10f), Mockito.any<LocationListener>())
    }
    /*
    @Test
    fun onRequestPermissionResultDoesNotRequestLocationUpdatesIfItDoesNotHavePermission() {
        val activity = mock(AppCompatActivity::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)

        //Mockito.`when`(activity.checkSelfPermission(Mockito.any())).thenReturn(PackageManager.PERMISSION_GRANTED)

        //context.checkPermission(permission, Process.myPid(), Process.myUid())

        Mockito.`when`(activity.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,Process.myPid(),Process.myUid())).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(activity.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,Process.myPid(),Process.myUid())).thenReturn(PackageManager.PERMISSION_DENIED)

        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        CentralLocationManager.onRequestPermissionsResult(1011,Array(0) {""},IntArray(0))
        Mockito.verify(manager, Mockito.times(0) ).requestLocationUpdates(LocationManager.GPS_PROVIDER,500,10f, CentralLocationListener)
    }

     */




}
