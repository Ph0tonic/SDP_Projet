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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.MockGroupDao
import ch.epfl.sdp.database.dao.OfflineHeatmapDao
import ch.epfl.sdp.database.dao.OfflineMarkerDao
import ch.epfl.sdp.database.dao.MockUserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DroneStatusFragmentTest {

    companion object {
        private const val DEFAULT_ALTITUDE_DISPLAY = " 0.0 m"
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
        private const val MAP_LOADING_TIMEOUT = 1000L
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
        // Firebase.database.goOffline()
        SearchGroupRepository.daoProvider = { MockGroupDao() }
        HeatmapRepository.daoProvider = { OfflineHeatmapDao() }
        MarkerRepository.daoProvider = { OfflineMarkerDao() }
        UserRepository.daoProvider = { MockUserDao() }

        SearchGroupRepositoryProvider.provide = { SearchGroupRepository() }
        HeatmapRepositoryProvider.provide = { HeatmapRepository() }
        MarkerRepositoryProvider.provide = { MarkerRepository() }
        UserRepositoryProvider.provide = { UserRepository() }

        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun updateDroneBatteryChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.batteryLevelLiveData.value = null
        }
        onView(withId(R.id.battery_level)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = 1F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 100%")))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = 0F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 0%")))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = 0.5F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 50%")))
    }

    @Test
    fun updateDroneAltitudeChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.absoluteAltitudeLiveData.value = null
        }

        onView(withId(R.id.altitude)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.absoluteAltitudeLiveData.value = 0F
        }
        onView(withId(R.id.altitude)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.absoluteAltitudeLiveData.value = 1.123F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 1.1 m")))

        runOnUiThread {
            Drone.absoluteAltitudeLiveData.value = 10F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 10.0 m")))
    }

    @Test
    fun updateDroneSpeedChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.speedLiveData.value = null
        }

        onView(withId(R.id.speed)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.speedLiveData.value = 0F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 0.0 m/s")))

        runOnUiThread {
            Drone.speedLiveData.value = 1.123F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 1.1 m/s")))

        runOnUiThread {
            Drone.speedLiveData.value = 5.2F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 5.2 m/s")))
    }

    @Test
    fun updateDronePositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            CentralLocationManager.currentUserPosition.value = LatLng(0.0, 0.0)
            Drone.positionLiveData.value = LatLng(0.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.positionLiveData.value = LatLng(1.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }

    @Test
    fun updateUserPositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.positionLiveData.value = LatLng(0.0, 0.0)
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
            Drone.batteryLevelLiveData.value = .00f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery1))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .10f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery2))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .30f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery3))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .50f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery4))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .70f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery5))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .90f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery6))))

        runOnUiThread {
            Drone.batteryLevelLiveData.value = .98f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery7))))
    }

    @Test
    fun updateDronePositionChangesDistToHome() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.homeLocationLiveData.value = Telemetry.Position(0.0, 0.0, 0f, 0f)
            Drone.positionLiveData.value = LatLng(0.0, 0.0)
        }

        onView(withId(R.id.distance_to_home)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.positionLiveData.value = LatLng(1.0, 0.0)
        }
        onView(withId(R.id.distance_to_home)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }

    @Test
    fun updateHomePositionChangesDistToHome() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.positionLiveData.value = LatLng(0.0, 0.0)
            Drone.homeLocationLiveData.value = Telemetry.Position(0.0, 0.0, 0f, 0f)
        }

        onView(withId(R.id.distance_to_home)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.homeLocationLiveData.value = Telemetry.Position(1.0, 0.0, 0f, 0f)
        }

        onView(withId(R.id.distance_to_home)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }
}
