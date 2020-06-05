package ch.epfl.sdp.ui.drone

import android.Manifest
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.MockGroupDao
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.dao.MockMarkerDao
import ch.epfl.sdp.database.dao.MockUserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data_manager.MainDataManager
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.drone.DroneInstanceMock
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import io.mavsdk.telemetry.Telemetry
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.hamcrest.Matchers.closeTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReturnDroneDialogFragmentTest {

    companion object {
        private const val MAP_LOADING_TIMEOUT = 1000L
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
        private const val EPSILON = 1e-9
    }

    private lateinit var mUiDevice: UiDevice
    private val intentWithGroupAndOperator = Intent()
            .putExtra(applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)
            .putExtra(applicationContext().getString(R.string.intent_key_role), Role.OPERATOR)

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
        MainDataManager.goOffline()
        DroneInstanceMock.setupDefaultMocks()

        // Do not use the real database, only use the offline version on the device
        // Firebase.database.goOffline()
        SearchGroupRepository.daoProvider = { MockGroupDao() }
        HeatmapRepository.daoProvider = { MockHeatmapDao() }
        MarkerRepository.daoProvider = { MockMarkerDao() }
        UserRepository.daoProvider = { MockUserDao() }

        SearchGroupRepositoryProvider.provide = { SearchGroupRepository() }
        HeatmapRepositoryProvider.provide = { HeatmapRepository() }
        MarkerRepositoryProvider.provide = { MarkerRepository() }
        UserRepositoryProvider.provide = { UserRepository() }

        //Fake login
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testLaunchDialogShowsReturnHomeDialog() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.return_drone_dialog))

        onView(withText(applicationContext().getString(R.string.return_drone_dialog_title)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnNegativeButtonClosesDialog() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.return_drone_dialog))

        onView(withText(applicationContext()
                .getString(R.string.cancel)))
                .perform(click())

        onView(withText(applicationContext().getString(R.string.return_drone_dialog_title)))
                .check(doesNotExist())
    }

    @Test
    fun testClickOnPositiveButtonShowsToast() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.return_drone_dialog))

        Drone.homeLocationLiveData.value = null

        // Click on return home
        onView(withText(MainApplication.applicationContext()
                .getString(R.string.return_drone_dialog_option_home)))
                .perform(click())

        // Test that the toast is displayed
        onView(withText(applicationContext().getString(R.string.drone_home_error)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnNeutralButtonShowsHomeSuccessToastWhenUserPositionIsNull() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.return_drone_dialog))

        runOnUiThread {
            Drone.homeLocationLiveData.value = Telemetry.Position(0.0, 0.0, 0.0f, 0.0f)
        }
        CentralLocationManager.currentUserPosition.value = null

        // Click on return user
        onView(withText(MainApplication.applicationContext()
                .getString(R.string.return_drone_dialog_option_user)))
                .perform(click())

        // Test that the toast is displayed
        onView(withText(applicationContext().getString(R.string.drone_home_success)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnNeutralButtonUpdatesMission() {
        val expectedLatLng = LatLng(47.397428, 8.545369) //Position of the drone before take off
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.return_drone_dialog))

        runOnUiThread {
            CentralLocationManager.currentUserPosition.value = expectedLatLng
        }

        // Click on return user
        onView(withText(MainApplication.applicationContext()
                .getString(R.string.return_drone_dialog_option_user)))
                .perform(click())

        assertThat(Drone.missionLiveData.value?.isEmpty(), Matchers.`is`(false))
        val returnToUserMission = Drone.missionLiveData.value?.get(0)
        val currentLat = returnToUserMission?.latitudeDeg
        val currentLong = returnToUserMission?.longitudeDeg

        assertThat(currentLat, Matchers.`is`(Matchers.notNullValue()))
        assertThat(currentLong, Matchers.`is`(Matchers.notNullValue()))

        //compare both position
        assertThat(currentLat, closeTo(expectedLatLng.latitude, EPSILON))
        assertThat(currentLong, closeTo(expectedLatLng.longitude, EPSILON))
        CentralLocationManager.currentUserPosition.value = null
    }
}