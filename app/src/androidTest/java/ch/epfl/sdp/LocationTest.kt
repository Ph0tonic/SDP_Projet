package ch.epfl.sdp

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationTest {
    private var mUiDevice: UiDevice? = null
    private var locationTestActivity: Activity? = null

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        locationTestActivity = LocationTestActivity()
    }

    private fun getContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }


}


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