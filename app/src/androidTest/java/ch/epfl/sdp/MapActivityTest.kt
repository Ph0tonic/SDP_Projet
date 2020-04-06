package ch.epfl.sdp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MapActivityTest {

    companion object {
        const val LATITUDE_TEST = 42.125
        const val LONGITUDE_TEST = -30.229
        const val ZOOM_TEST = 0.9
        const val MAP_LOADING_TIMEOUT = 1000L
        const val EPSILON = 1e-10
    }

    lateinit var preferencesEditor: SharedPreferences.Editor
    private lateinit var mUiDevice: UiDevice

    @get:Rule
    var mActivityRule = ActivityTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(getInstrumentation())
    }

    @Before
    fun setUp() {
        val targetContext: Context = getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun canStartMission() {
        // Launch activity
        mActivityRule.launchActivity(Intent())
        // Add 4 points to the map for the strategy
        runOnUiThread {
            arrayListOf(
                    LatLng(8.543434, 47.398979),
                    LatLng(8.543934, 47.398279),
                    LatLng(8.544867, 47.397426),
                    LatLng(8.543067, 47.397026)
            ).forEach { latLng -> mActivityRule.activity.onMapClicked(latLng) }
        }
        onView(withId(R.id.start_mission_button)).perform(click())
    }

    @Test
    fun mapboxUsesOurPreferences() {
        preferencesEditor
                .putString("latitude", LATITUDE_TEST.toString())
                .putString("longitude", LONGITUDE_TEST.toString())
                .putString("zoom", ZOOM_TEST.toString())
                .apply()

        // Launch activity after setting preferences
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.latitude, closeTo(LATITUDE_TEST, EPSILON))
                assertThat(mapboxMap.cameraPosition.target.longitude, closeTo(LONGITUDE_TEST, EPSILON))
                assertThat(mapboxMap.cameraPosition.zoom, closeTo(ZOOM_TEST, EPSILON))
            }
        }
    }

    @Test
    fun mapBoxCanAddPointToHeatMap() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(MapActivity.MAP_READY_DESCRIPTION)), MAP_LOADING_TIMEOUT);
        runOnUiThread{
            mActivityRule.activity.addPointToHeatMap(10.0, 10.0)
        }
    }

    @Test
    fun canUpdateUserLocation() {
        CentralLocationManager.currentUserPosition.postValue(LatLng(LATITUDE_TEST, LONGITUDE_TEST))
    }

    @Test
    fun canUpdateUserLocationTwice() {
        CentralLocationManager.currentUserPosition.postValue(LatLng(LATITUDE_TEST, LONGITUDE_TEST))
        CentralLocationManager.currentUserPosition.postValue(LatLng(-LATITUDE_TEST, -LONGITUDE_TEST))
    }

    @Test
    fun canOnRequestPermissionResult() {
        mActivityRule.launchActivity(Intent())
        mActivityRule.activity.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
    }

    @Test
    fun droneStatusIsVisible(){
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.drone_status)).check(matches(isDisplayed()))
    }

    @Test
    fun clickOnMapAddsWaypoint() {
        mActivityRule.launchActivity(Intent())

        assertThat(mActivityRule.activity.waypoints.size, equalTo(0))

        // Wait for the map to load
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000)

        // Add a point
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
        }

        assertThat(mActivityRule.activity.waypoints.size, equalTo(1))
    }
}