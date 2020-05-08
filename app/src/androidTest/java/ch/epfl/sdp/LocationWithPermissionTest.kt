package ch.epfl.sdp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.dao.MockMarkerDao
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class LocationWithPermissionTest {
    private var mUiDevice: UiDevice? = null

    companion object {
        private const val DUMMY_GROUP_ID = "DummyGroupId"
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
    }

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val intentWithGroupAndOperator = Intent()
            .putExtra(applicationContext().getString(R.string.INTENT_KEY_GROUP_ID), DUMMY_GROUP_ID)
            .putExtra(applicationContext().getString(R.string.INTENT_KEY_ROLE), Role.OPERATOR)

    @get:Rule
    val mActivityRule = IntentsTestRule(MapActivity::class.java, true, false)

    @Before
    @Throws(Exception::class)
    fun before() {
        //Fake logged in
        UiThreadStatement.runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        HeatmapRepository.daoProvider = { MockHeatmapDao() }
        MarkerRepository.daoProvider = { MockMarkerDao() }
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun centralLocationManagerRequestsLocationUpdatesIfItHasPermission() {
        val activity = mock(AppCompatActivity::class.java)
        val context = mock(Context::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(activity.applicationContext).thenReturn(context)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        Mockito.verify(manager).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER), Mockito.eq(500L), Mockito.eq(10f), Mockito.any<LocationListener>())
    }

    @Test
    fun checkLocationReturnsTrueIfLocationIsEnabled() {
        CentralLocationManager.configure(mActivityRule.activity)
        assertThat(CentralLocationManager.checkLocationSetting(), equalTo(true))
    }

    @Test
    fun onRequestPermissionResultRequestsLocationUpdatesIfItHasPermission() {
        val activity = mock(AppCompatActivity::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(activity.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(activity.checkSelfPermission(Mockito.any())).thenReturn(PackageManager.PERMISSION_GRANTED)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity)
        CentralLocationManager.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
        Mockito.verify(manager, Mockito.times(2)).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER), Mockito.eq(500L), Mockito.eq(10f), Mockito.any<LocationListener>())
    }
}
