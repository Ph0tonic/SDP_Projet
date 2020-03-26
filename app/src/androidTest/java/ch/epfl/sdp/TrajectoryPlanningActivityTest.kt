package ch.epfl.sdp

import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.ui.missionDesign.TrajectoryPlanningActivity
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrajectoryPlanningActivityTest {

    private var mUiDevice: UiDevice? = null

    @get:Rule
    val mActivityRule = IntentsTestRule(TrajectoryPlanningActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun clickOnMapAddsWaypoint(){
        assertThat(mActivityRule.activity.waypoints.size,equalTo(0))
        onView(withId(R.id.trajectory_planning_mapView))
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())
        assertThat(mActivityRule.activity.waypoints.size,equalTo(1))
    }

    @Test
    fun resultIntentHasCorrectExtra(){
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000);

        // Click twice
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())

        // Leave map
        onView(withId(R.id.mission_design_button_done)).perform(click())

        val wayPoints = mActivityRule.activity.waypoints

        assertThat(mActivityRule.getActivityResult(), hasResultCode(Activity.RESULT_OK))
        assertThat(mActivityRule.getActivityResult(), hasResultData(IntentMatchers.hasExtraWithKey("waypoints")))
    }

    @Test
    fun intentReturnsCorrectListOfWaypoints(){
        // Wait that map loads
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), 1000);

        // Click twice
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())

        val wayPoints = mActivityRule.activity.waypoints

        // Leave map
        onView(withId(R.id.mission_design_button_done)).perform(click())

        assertThat(mActivityRule.getActivityResult(), hasResultData(IntentMatchers.hasExtra("waypoints", wayPoints)))
    }

    @Test
    fun mapIsVisible(){
        onView(withId(R.id.trajectory_planning_mapView)).check(matches(isDisplayed()))
    }
}