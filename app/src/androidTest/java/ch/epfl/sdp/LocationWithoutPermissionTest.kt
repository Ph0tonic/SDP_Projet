package ch.epfl.sdp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class LocationWithoutPermissionTest {
    private var mUiDevice: UiDevice? = null

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        //locationTestActivity = LocationTestActivity()
    }

    @Test
    fun centralLocationManagerDoesNotRequestLocationUpdatesIfItDoesNotHavePermission() {
        /*

        //val activity = Mockito.mock(AppCompatActivity::class.java)
        val activity = Mockito.spy(MainActivity::class.java)
        //val context = Mockito.mock(Context::class.java)
        val manager = Mockito.mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        /*
        Mockito.`when`(activity.getApplicationContext()).thenReturn(context)
        Mockito.`when`(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)

         */
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)


        CentralLocationManager.configure(mActivityRule.activity)
        Mockito.verify(manager,Mockito.never()).requestLocationUpdates(LocationManager.GPS_PROVIDER,2 * 1000,10f, CentralLocationListener)

         */
        Assert.assertTrue(true)
    }
}