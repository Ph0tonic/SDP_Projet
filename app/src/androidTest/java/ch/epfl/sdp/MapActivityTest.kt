package ch.epfl.sdp

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
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
import ch.epfl.sdp.utils.Auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
        // TODO change to latLng for simplicity
        private val FAKE_LOCATION_TEST = LatLng(42.125, -30.229)
        private const val FAKE_HEATMAP_POINT_INTENSITY = 8.12

        private const val ZOOM_TEST = 0.9
        private const val MAP_LOADING_TIMEOUT = 1000L
        private const val EPSILON = 1e-9
        private const val DEFAULT_ALTITUDE = " 0.0 m"
        private const val GROUP_ID_PROPERTY_NAME_FOR_INTENT = "groupId"
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
    }

    private lateinit var preferencesEditor: SharedPreferences.Editor
    private lateinit var mUiDevice: UiDevice
    private val intentWithGroup = Intent().putExtra(GROUP_ID_PROPERTY_NAME_FOR_INTENT, DUMMY_GROUP_ID)

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
        //Fake logged in
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        Firebase.database.goOffline()
        //HeatmapRepository.daoProvider = { MockHeatmapDao() }
        //MarkerRepository.daoProvider = { MockMarkerDao() }
        mUiDevice = UiDevice.getInstance(getInstrumentation())

        val targetContext: Context = getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun canStartMission() {
        // Launch activity
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

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
                .putString(applicationContext().getString(R.string.prefs_latitude), FAKE_LOCATION_TEST.latitude.toString())
                .putString(applicationContext().getString(R.string.prefs_longitude), FAKE_LOCATION_TEST.longitude.toString())
                .putString(applicationContext().getString(R.string.prefs_zoom), ZOOM_TEST.toString())
                .apply()

        // Launch activity after setting preferences
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                Log.w("TESTS", mapboxMap.cameraPosition.target.distanceTo(FAKE_LOCATION_TEST).toString())
                assertThat(mapboxMap.cameraPosition.target.distanceTo(FAKE_LOCATION_TEST), closeTo(0.0, EPSILON))
                assertThat(mapboxMap.cameraPosition.zoom, closeTo(ZOOM_TEST, EPSILON))
            }
        }
    }

    @Test
    fun addPointToHeatmapAddsPointToHeatmap() {
        // Launch activity after setting preferences
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        assertThat(mActivityRule.activity.heatmapRepository.getGroupHeatmaps(DUMMY_GROUP_ID).value?.size, equalTo(0))

        runOnUiThread {
            mActivityRule.activity.addPointToHeatMap(FAKE_LOCATION_TEST, FAKE_HEATMAP_POINT_INTENSITY)
        }
        val heatmaps = mActivityRule.activity.heatmapRepository.getGroupHeatmaps(DUMMY_GROUP_ID)
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]?.value?.dataPoints?.size, equalTo(1))
    }

    @Test
    fun canUpdateUserLocation() {
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
    }

    @Test
    fun canUpdateUserLocationTwice() {
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
    }

    @Test
    fun canOnRequestPermissionResult() {
        mActivityRule.launchActivity(intentWithGroup)
        mActivityRule.activity.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
    }

    @Test
    fun droneStatusIsVisible() {
        mActivityRule.launchActivity(intentWithGroup)
        onView(withId(R.id.drone_status)).check(matches(isDisplayed()))
    }

    @Test
    fun longClickOnMapAddAMarker() {
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        onView(withId(R.id.mapView)).perform(longClick())
        runOnUiThread {
            assertThat(mActivityRule.activity.victimMarkers.size, equalTo(1))
        }
        onView(withId(R.id.mapView)).perform(longClick())
        Thread.sleep(2000)
        runOnUiThread {
            assertThat(mActivityRule.activity.victimMarkers.size, equalTo(0))
        }
    }

    @Test
    fun clickOnMapInteractWithMapBoxSearchAreaBuilder() {
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

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
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

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
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

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
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        onView(withId(R.id.floating_menu_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())

        intended(hasComponent(OfflineManagerActivity::class.java.name))
    }

    @Test
    fun locateButtonIsWorking() {
        mActivityRule.launchActivity(intentWithGroup)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        runOnUiThread {
            Drone.currentPositionLiveData.value = FAKE_LOCATION_TEST
        }

        onView(withId(R.id.floating_menu_button)).perform(click())
        onView(withId(R.id.locate_button)).perform(click())

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.distanceTo(FAKE_LOCATION_TEST), closeTo(0.0, EPSILON))
            }
        }
    }

    @Test
    fun updateDroneBatteryChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroup)

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
        mActivityRule.launchActivity(intentWithGroup)

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
        mActivityRule.launchActivity(intentWithGroup)

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
        mActivityRule.launchActivity(intentWithGroup)
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
        mActivityRule.launchActivity(intentWithGroup)
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
        mActivityRule.launchActivity(intentWithGroup)
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