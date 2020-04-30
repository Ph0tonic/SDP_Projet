package ch.epfl.sdp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrajectoryPlanningActivityTest {

    private lateinit var mUiDevice: UiDevice

    companion object {
        const val MAP_LOADING_TIMEOUT = 1000L
    }

    @get:Rule
    val mActivityRule = IntentsTestRule(MapActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun clickOnMapInteractWithBuilder() {
        // Wait for the map to load
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == MainApplication.applicationContext().getString(R.string.map_ready), Matchers.`is`(true))

        val builder = mActivityRule.activity.searchAreaBuilder
        assertThat(builder.vertices.size, equalTo(0))

        // Add a point
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
        }

        assertThat(builder.vertices.size, equalTo(1))
    }

    /*
    @Test
    fun resultIntentHasCorrectExtra(){
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000);

        // Click twice
        onView(withId(R.id.mapView)).perform(click())
        onView(withId(R.id.mapView)).perform(click())

        // Leave map
        onView(withId(R.id.mission_design_button_done)).perform(click())

        assertThat(mActivityRule.activityResult, hasResultCode(Activity.RESULT_OK))
        assertThat(mActivityRule.activityResult, hasResultData(IntentMatchers.hasExtraWithKey("waypoints")))
    }

    @Test
    fun intentReturnsCorrectListOfWaypoints(){
        // Wait that map loads
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000);

        // Click twice
        onView(withId(R.id.mapView)).perform(click())
        onView(withId(R.id.mapView)).perform(click())

        val wayPoints = mActivityRule.activity.waypoints

        // Leave map
        onView(withId(R.id.mission_design_button_done)).perform(click())

        assertThat(mActivityRule.activityResult, hasResultData(IntentMatchers.hasExtra("waypoints", wayPoints)))
    }

    @Test
    fun clearWaypointsClearsWaypoints(){
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000);
        onView(withId(R.id.mapView)).perform(click())
        onView(withId(R.id.mission_design_button_clear_waypoint)).perform(click())
        assertThat(mActivityRule.activity.waypoints.size, equalTo(0))
    }*/

    @Test
    fun mapIsVisible() {
        onView(withId(R.id.mapView)).check(matches(isDisplayed()))
    }
}