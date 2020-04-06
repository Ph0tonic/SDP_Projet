package ch.epfl.sdp

import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AaaLocationWithoutPermissionTest {
    private lateinit var mUiDevice: UiDevice

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        //locationTestActivity = LocationTestActivity()
    }
    /*
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
    }
    */
    @Test
    fun configureWithoutPermissionsOpensDialog() {
        CentralLocationManager.configure(mActivityRule.activity)

        val allowPermissions: UiObject = mUiDevice.findObject(UiSelector().textMatches("(?i)deny"))
        if (allowPermissions.exists()) {
            allowPermissions.click()
        }else{
            Assert.fail("Permission Dialog doesn't exist")
        }
    }
}