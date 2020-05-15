package ch.epfl.sdp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.annotation.UiThreadTest
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.dao.MockMarkerDao
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.ui.maps.MapActivity
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
        private const val REFRESH_RATE = 500L
        private const val MIN_DIST = 10f
    }

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val intentWithGroupAndOperator = Intent()
            .putExtra(applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)
            .putExtra(applicationContext().getString(R.string.intent_key_role), Role.OPERATOR)

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
        Mockito.`when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity, context)
        Mockito.verify(manager).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER), Mockito.eq(REFRESH_RATE), Mockito.eq(MIN_DIST), Mockito.any<LocationListener>())
    }

    @UiThreadTest
    fun checkLocationReturnsTrueIfLocationIsEnabled() {
        //Found help here : https://github.com/skyisle/android-test-kit/issues/121
        CentralLocationManager.configure(mActivityRule.activity)
        assertThat(CentralLocationManager.checkLocationSetting(), equalTo(true))
    }

    @Test
    fun onRequestPermissionResultRequestsLocationUpdatesIfItHasPermission() {
        val activity = mock(AppCompatActivity::class.java)
        val context = mock(Context::class.java)
        val manager = mock(LocationManager::class.java)
        Mockito.`when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(manager)
        Mockito.`when`(context.checkSelfPermission(Mockito.any())).thenReturn(PackageManager.PERMISSION_GRANTED)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        CentralLocationManager.configure(activity, context)
        CentralLocationManager.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
        Mockito.verify(manager, Mockito.times(2)).requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER), Mockito.eq(REFRESH_RATE), Mockito.eq(MIN_DIST), Mockito.any<LocationListener>())
    }
}
