package ch.epfl.sdp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import ch.epfl.sdp.drone.Drone
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.activity_map.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

const val LATITUDE_TEST = "42.125"
const val LONGITUDE_TEST = "-30.229"
const val ZOOM_TEST = "0.9"

@RunWith(AndroidJUnit4::class)
class MapActivityTest {
    var preferencesEditor: SharedPreferences.Editor? = null

    @get:Rule
    var mActivityRule = ActivityTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Before
    fun setUp() {
        val targetContext: Context = getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun mapboxUseOurPreferences() {
        preferencesEditor!!
                .putString("latitude", LATITUDE_TEST)
                .putString("longitude", LONGITUDE_TEST)
                .putString("zoom", ZOOM_TEST)
                .apply();

        // Launch activity
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assert(mapboxMap.cameraPosition.target.latitude.toString() == LATITUDE_TEST)
                assert(mapboxMap.cameraPosition.target.longitude.toString() == LONGITUDE_TEST)
                assert(mapboxMap.cameraPosition.zoom.toString() == ZOOM_TEST)
            }

            Drone.currentPositionLiveData.postValue(LatLng(47.398039859999997, 8.5455725400000002))
            Drone.currentPositionLiveData.postValue(LatLng(47.398039859999997, 8.5455725400000002))
        }
    }
    @Test
    fun mapBoxCanAddMarker(){
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.mapView)).perform(click())
        Thread.sleep(1000)
        //click on the current marker once again to remove it
        onView(withId(R.id.mapView)).perform(click())
    }
    @Test
    fun mapBoxCanRemoveMarker() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.mapView)).perform(doubleClick())
    }

    @Test
    fun canStartMission(){
        // Launch activity
        mActivityRule.launchActivity(Intent())

        Espresso.onView(withId(R.id.start_mission_button)).perform(ViewActions.click())

    }
}