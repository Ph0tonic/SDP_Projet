package ch.epfl.sdp

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
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.ui.maps.ReturnDroneDialogFragment
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.google.type.LatLng
import io.mavsdk.telemetry.Telemetry
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
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
        //Fake login
        UiThreadStatement.runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testLaunchDialogShowsIt() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.ReturnDroneDialogFragment))

        onView(withText(applicationContext().getString(R.string.ReturnDroneDialogTitle)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnNegativeButtonClosesDialog() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.ReturnDroneDialogFragment))

        onView(withText(applicationContext()
                .getString(R.string.dialog_negative_button)))
                .perform(click())

        onView(withText(applicationContext().getString(R.string.ReturnDroneDialogTitle)))
                .check(doesNotExist())
    }

    @Test
    fun testClickOnPositiveButtonShowsToast() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.ReturnDroneDialogFragment))

        Drone.currentHomeLiveData.value = null

        // Click on return home
        onView(withText(MainApplication.applicationContext()
                .getString(R.string.ReturnDroneDialogHome)))
                .perform(click())

        // Test that the toast is displayed
        onView(withText(applicationContext().getString(R.string.drone_home_error)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testClickOnNeutralButtonShowsToast() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), Matchers.equalTo(true))

        // Show Dialog
        ReturnDroneDialogFragment().show(mActivityRule.activity.supportFragmentManager, mActivityRule.activity.getString(R.string.ReturnDroneDialogFragment))

        CentralLocationManager.currentUserPosition.value = null

        // Click on return user
        onView(withText(MainApplication.applicationContext()
                .getString(R.string.ReturnDroneDialogUser)))
                .perform(click())

        // Test that the toast is displayed
        onView(withText(applicationContext().getString(R.string.drone_user_error)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }
}