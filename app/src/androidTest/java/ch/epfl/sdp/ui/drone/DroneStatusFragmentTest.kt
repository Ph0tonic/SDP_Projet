package ch.epfl.sdp.ui.drone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.dao.MockMarkerDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DroneStatusFragmentTest {

    companion object {
        private const val DEFAULT_ALTITUDE_DISPLAY = " 0.0 m"
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
    }

    private lateinit var preferencesEditor: SharedPreferences.Editor
    private lateinit var mUiDevice: UiDevice
    private val intentWithGroupAndOperator = Intent()
            .putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)
            .putExtra(MainApplication.applicationContext().getString(R.string.intent_key_role), Role.OPERATOR)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mActivityRule = IntentsTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    @Throws(Exception::class)
    fun before() {
        //Fake login
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        // Do not use the real database, only use the offline version on the device
        //Firebase.database.goOffline()
        HeatmapRepository.daoProvider = { MockHeatmapDao() }
        MarkerRepository.daoProvider = { MockMarkerDao() }
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }


    @Test
    fun updateDroneBatteryChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = null
        }
        onView(withId(R.id.battery_level)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 1F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 100%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 0F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 0%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 0.5F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 50%")))
    }

    @Test
    fun updateDroneAltitudeChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = null
        }

        onView(withId(R.id.altitude)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 0F
        }
        onView(withId(R.id.altitude)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 1.123F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 1.1 m")))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 10F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 10.0 m")))
    }

    @Test
    fun updateDroneSpeedChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentSpeedLiveData.value = null
        }

        onView(withId(R.id.speed)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 0F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 0.0 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 1.123F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 1.1 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 5.2F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 5.2 m/s")))
    }

    @Test
    fun updateDronePositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        runOnUiThread {
            CentralLocationManager.currentUserPosition.value = LatLng(0.0, 0.0)
            Drone.currentPositionLiveData.value = LatLng(0.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.currentPositionLiveData.value = LatLng(1.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }

    @Test
    fun updateUserPositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        runOnUiThread {
            Drone.currentPositionLiveData.value = LatLng(0.0, 0.0)
            CentralLocationManager.currentUserPosition.value = LatLng(0.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            CentralLocationManager.currentUserPosition.value = LatLng(1.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }

    @Test
    fun updateBatteryLevelChangesBatteryLevelIcon() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .00f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery1))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .10f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery2))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .30f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery3))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .50f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery4))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .70f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery5))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .90f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery6))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .98f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery7))))
    }
}