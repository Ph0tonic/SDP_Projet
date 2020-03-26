package ch.epfl.sdp

import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.ui.missionDesign.TrajectoryPlanningActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrajectoryPlanningActivityTest {

    @get:Rule
    val mActivityRule = IntentsTestRule(TrajectoryPlanningActivity::class.java)

    @Test
    fun clickOnMapAddsWaypoint(){
        assertThat(mActivityRule.activity.waypoints.size,equalTo(0))
        onView(withId(R.id.trajectory_planning_mapView))
        onView(withId(R.id.trajectory_planning_mapView)).perform(click())
        assertThat(mActivityRule.activity.waypoints.size,equalTo(1))
    }

    @Test
    fun resultIntentHasCorrectExtra(){
        // Wait that map loads
        onView(withId(R.id.trajectory_planning_mapView))

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
        onView(withId(R.id.trajectory_planning_mapView))

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