package ch.epfl.sdp

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.utils.CentralLocationManager
import ch.epfl.sdp.drone.Drone.currentMissionLiveData
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineManagerActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers.*
import org.junit.Assert
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
        const val MAP_LOADING_TIMEOUT = 30000L
        const val EPSILON = 1e-10
        const val DEFAULT_ALTITUDE = " 0.0 m"
    }

    private lateinit var preferencesEditor: SharedPreferences.Editor
    private lateinit var mUiDevice: UiDevice

    @get:Rule
    var mActivityRule = IntentsTestRule(
            MapActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = grant(permission.ACCESS_FINE_LOCATION, permission.ACCESS_FINE_LOCATION)

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
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))
        onView(withId(R.id.start_or_return_button)).perform(click())
        val expectedLatLng = LatLng(47.397026, 8.543067)

        // Add 4 points to the map for the strategy
        runOnUiThread {
            mActivityRule.activity.searchAreaBuilder.reset()
            arrayListOf(
                    LatLng(47.398979, 8.543434),
                    LatLng(47.398279, 8.543934),
                    LatLng(47.397426, 8.544867),
                    expectedLatLng //we consider the closest point to the drone
            ).forEach { latLng -> mActivityRule.activity.onMapClicked(latLng) }
        }

        onView(withId(R.id.start_or_return_button)).perform(click())

        val uploadedMission = currentMissionLiveData.value

        if (uploadedMission != null) {
            assertThat(expectedLatLng.latitude, closeTo(uploadedMission[0].latitudeDeg, 0.1))
            assertThat(expectedLatLng.longitude, closeTo(uploadedMission[0].longitudeDeg, 0.1))
        } else {
            Assert.fail("No MissionItem")
        }
    }

    @Test
    fun mapboxUsesOurPreferences() {
        preferencesEditor
                .putString(applicationContext().getString(R.string.prefs_latitude), LATITUDE_TEST.toString())
                .putString(applicationContext().getString(R.string.prefs_longitude), LONGITUDE_TEST.toString())
                .putString(applicationContext().getString(R.string.prefs_zoom), ZOOM_TEST.toString())
                .apply()

        // Launch activity after setting preferences
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))


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
        assertThat(mActivityRule.activity.heatmapFeatures.size, equalTo(0))

        // Wait for the map to load and add a heatmap point
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        runOnUiThread {
            mActivityRule.activity.addPointToHeatMap(10.0, 10.0, 10.0)
        }
        assertThat(mActivityRule.activity.heatmapFeatures.size, equalTo(1))
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
    fun droneStatusIsVisible() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.drone_status)).check(matches(isDisplayed()))
    }

    @Test
    fun longClickOnMapAddAMarker() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)

        onView(withId(R.id.mapView)).perform(longClick())
        runOnUiThread {
            assertThat(mActivityRule.activity.victimMarkers.size, equalTo(1))
        }
        onView(withId(R.id.mapView)).perform(longClick())
        runOnUiThread {
            assertThat(mActivityRule.activity.victimMarkers.size, equalTo(0))
        }
    }

    @Test
    fun clickOnMapInteractWithMapBoxSearchAreaBuilder() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        val searchAreaBuilder = mActivityRule.activity.searchAreaBuilder

        // Add a point
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
        }

        assertThat(searchAreaBuilder.vertices.size, equalTo(1))
        runOnUiThread {
            searchAreaBuilder.reset()
        }
        assertThat(searchAreaBuilder.vertices.size, equalTo(0))
    }

    @Test
    fun whenExceptionAppendInSearchAreaBuilderAToastIsDisplayed() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        // Add 5 points
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
            mActivityRule.activity.onMapClicked(LatLng(1.0, 1.0))
            mActivityRule.activity.onMapClicked(LatLng(2.0, 2.0))
            mActivityRule.activity.onMapClicked(LatLng(3.0, 3.0))
            mActivityRule.activity.onMapClicked(LatLng(4.0, 4.0))
        }

        onView(withText("Already enough points"))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun deleteButtonRemovesWaypoints() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        val searchAreaBuilder = mActivityRule.activity.searchAreaBuilder
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
        }
        assertThat(searchAreaBuilder.vertices.size, equalTo(1))

        onView(withId(R.id.floating_menu_button)).perform(click())
        while (mActivityRule.activity.searchAreaBuilder.vertices.isNotEmpty()) onView(withId(R.id.clear_button)).perform(click()) //button is not instantly visible because it is appearing, so we try to click until we success.

        runOnUiThread {
            assertThat(mActivityRule.activity.searchAreaBuilder.vertices.isEmpty(), equalTo(true))
        }
    }

    @Test
    fun storeMapButtonIsWorking() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        onView(withId(R.id.floating_menu_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())

        intended(hasComponent(OfflineManagerActivity::class.java.name))
    }

    @Test
    fun locateButtonIsWorking() {
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        runOnUiThread {
            Drone.currentPositionLiveData.postValue(LatLng(LATITUDE_TEST, LONGITUDE_TEST))
        }

        onView(withId(R.id.floating_menu_button)).perform(click())
        onView(withId(R.id.locate_button)).perform(click())
        onView(withId(R.id.locate_button)).perform(click())

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.latitude, closeTo(LATITUDE_TEST, EPSILON))
                assertThat(mapboxMap.cameraPosition.target.longitude, closeTo(LONGITUDE_TEST, EPSILON))
            }
        }
    }

    @Test
    fun updateDroneBatteryChangesDroneStatus() {
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(null)
        }
        onView(withId(R.id.battery_level)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(1F)
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 100%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(0F)
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 0%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(0.5F)
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 50%")))
    }

    @Test
    fun updateDroneAltitudeChangesDroneStatus() {
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.postValue(null)
        }

        onView(withId(R.id.altitude)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.postValue(0F)
        }
        onView(withId(R.id.altitude)).check(matches(withText(DEFAULT_ALTITUDE)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.postValue(1.123F)
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 1.1 m")))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.postValue(10F)
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 10.0 m")))
    }

    @Test
    fun updateDroneSpeedChangesDroneStatus() {
        mActivityRule.launchActivity(Intent())

        runOnUiThread {
            Drone.currentSpeedLiveData.postValue(null)
        }

        onView(withId(R.id.speed)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentSpeedLiveData.postValue(0F)
        }
        onView(withId(R.id.speed)).check(matches(withText(" 0.0 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.postValue(1.123F)
        }
        onView(withId(R.id.speed)).check(matches(withText(" 1.1 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.postValue(5.2F)
        }
        onView(withId(R.id.speed)).check(matches(withText(" 5.2 m/s")))
    }

    @Test
    fun updateDronePositionChangesDistToUser() {
        mActivityRule.launchActivity(Intent())
        runOnUiThread {
            CentralLocationManager.currentUserPosition.postValue(LatLng(0.0, 0.0))
            Drone.currentPositionLiveData.postValue(LatLng(0.0, 0.0))
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE)))

        runOnUiThread {
            Drone.currentPositionLiveData.postValue(LatLng(1.0, 0.0))
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE))))
    }

    @Test
    fun updateUserPositionChangesDistToUser() {
        mActivityRule.launchActivity(Intent())
        runOnUiThread {
            Drone.currentPositionLiveData.postValue(LatLng(0.0, 0.0))
            CentralLocationManager.currentUserPosition.postValue(LatLng(0.0, 0.0))
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE)))

        runOnUiThread {
            CentralLocationManager.currentUserPosition.postValue(LatLng(1.0, 0.0))
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE))))
    }

    @Test
    fun updateBatteryLevelChangesBatteryLevelIcon() {
        mActivityRule.launchActivity(Intent())
        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.00f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery1))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.10f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery2))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.30f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery3))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.50f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery4))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.70f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery5))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.90f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery6))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.postValue(.98f)
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery7))))
    }
}