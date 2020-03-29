package ch.epfl.sdp

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy


@RunWith(AndroidJUnit4::class)
class LocationTest {
    private var mUiDevice: UiDevice? = null
    //private var locationTestActivity: Activity? = null

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

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
        //val activity = spy(mActivityRule.activity)
        val activity = mock(AppCompatActivity::class.java)
        //val activity = mActivityRule.activity
        val context = mock(Context::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(activity.getApplicationContext()).thenReturn(context)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        Mockito.verify(manager).requestLocationUpdates(LocationManager.GPS_PROVIDER,2 * 1000,10f, CentralLocationListener)
        Assert.assertTrue(true)
    }

}

/*
private class LocationTestActivity: Activity(), LocationSubscriber {
    private var locationChanged = false
        get() = field
        set(value) {
            field = value
        }

    override fun onStart() {
        super.onStart()
        locationChanged = false

    }

    override fun onLocationChanged(location: Location) {
        locationChanged = true
    }

}
*/
