package ch.epfl.sdp

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import ch.epfl.sdp.database.dao.MockHeatmapDao
import ch.epfl.sdp.database.dao.MockMarkerDao
import ch.epfl.sdp.database.data.HeatmapData
import ch.epfl.sdp.database.data.HeatmapPointData
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.mission.SimpleQuadStrategy
import ch.epfl.sdp.mission.SpiralStrategy
import ch.epfl.sdp.searcharea.QuadrilateralArea
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.ui.maps.offline.OfflineManagerActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapActivityTest {

    companion object {
        private val FAKE_LOCATION_TEST = LatLng(42.125, -30.229)
        private const val FAKE_HEATMAP_POINT_INTENSITY = 8.12

        private const val ZOOM_TEST = 0.9
        private const val MAP_LOADING_TIMEOUT = 1000L
        private const val EPSILON = 1e-9
        private const val DRONE_ALTITUDE = 20.0F
        private const val DEFAULT_ALTITUDE_DISPLAY = " 0.0 m"
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
    }

    private lateinit var preferencesEditor: SharedPreferences.Editor
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
    val grantPermissionRule: GrantPermissionRule = grant(permission.ACCESS_FINE_LOCATION, permission.ACCESS_FINE_LOCATION)

    @Before
    @Throws(Exception::class)
    fun before() {
        //Fake login
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        // Do not use the real database, only use the offline version on the device
        //Firebase.database.goOffline()
        HeatmapRepository.daoProvider = { MockHeatmapDao() }
        MarkerRepository.daoProvider = { MockMarkerDao() }
        mUiDevice = UiDevice.getInstance(getInstrumentation())

        val targetContext: Context = getInstrumentation().targetContext
        preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    @Test
    fun clickingOnLaunchMissionStartAndGenerateAMission() {
        preferencesEditor
                .putString(applicationContext().getString(R.string.prefs_drone_altitude), DRONE_ALTITUDE.toString())
                .apply()

        // Launch activity
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        // TODO: Why click on menu button doesn't work ?
        // Open menu to click on start button
        onView(withId(R.id.start_or_return_button)).perform(click())

        runOnUiThread {
            val searchArea = QuadrilateralArea(arrayListOf(
                    LatLng(47.397026, 8.543067), //we consider the closest point to the drone
                    LatLng(47.398979, 8.543434),
                    LatLng(47.398279, 8.543934),
                    LatLng(47.397426, 8.544867)
            ))
            mActivityRule.activity.missionBuilder
                    .withSearchArea(searchArea)
                    .withStartingLocation(LatLng(47.397026, 8.543067))
                    .withStrategy(SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE))
        }

        // Then start mission officially
        onView(withId(R.id.start_or_return_button)).perform(click())
        runOnUiThread {
            mActivityRule.activity.launchMission()
        }

        val uploadedMission = Drone.currentMissionLiveData.value

        assertThat(uploadedMission, `is`(notNullValue()))
        assertThat(uploadedMission!!.size, not(equalTo(0)))
    }

    @Test
    fun mapboxUsesOurPreferences() {
        preferencesEditor
                .putString(applicationContext().getString(R.string.prefs_latitude), FAKE_LOCATION_TEST.latitude.toString())
                .putString(applicationContext().getString(R.string.prefs_longitude), FAKE_LOCATION_TEST.longitude.toString())
                .putString(applicationContext().getString(R.string.prefs_zoom), ZOOM_TEST.toString())
                .apply()

        // Launch activity after setting preferences
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.distanceTo(FAKE_LOCATION_TEST), closeTo(0.0, EPSILON))
                assertThat(mapboxMap.cameraPosition.zoom, closeTo(ZOOM_TEST, EPSILON))
            }
        }
    }

    @Test
    fun addPointToHeatmapAddsPointToHeatmap() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        assertThat(mActivityRule.activity.heatmapManager.getGroupHeatmaps(DUMMY_GROUP_ID).value?.size, equalTo(0))

        runOnUiThread {
            mActivityRule.activity.addPointToHeatMap(FAKE_LOCATION_TEST, FAKE_HEATMAP_POINT_INTENSITY)
        }
        val heatmaps = mActivityRule.activity.heatmapManager.getGroupHeatmaps(DUMMY_GROUP_ID)
        assertThat(heatmaps.value, `is`(notNullValue()))

        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID], `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value, `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value!!.dataPoints, `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value!!.dataPoints.size, equalTo(1))
    }

    @Test
    fun heatmapPaintersAreGeneratedWhenLaunchingApp() {
        val heatmapDao = MockHeatmapDao()
        val heatmap = HeatmapData(mutableListOf(
                HeatmapPointData(LatLng(41.0, 10.0), 10.0),
                HeatmapPointData(LatLng(41.0, 10.0), 8.5)
        ), FAKE_ACCOUNT_ID)
        heatmapDao.updateHeatmap(DUMMY_GROUP_ID, heatmap)
        HeatmapRepository.daoProvider = { heatmapDao }

        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        assertThat(mActivityRule.activity.heatmapManager.getGroupHeatmaps(DUMMY_GROUP_ID).value?.size, equalTo(1))

        val heatmaps = mActivityRule.activity.heatmapManager.getGroupHeatmaps(DUMMY_GROUP_ID)
        assertThat(heatmaps.value, `is`(notNullValue()))

        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID], `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value, `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value!!.dataPoints, `is`(notNullValue()))
        assertThat(heatmaps.value!![FAKE_ACCOUNT_ID]!!.value!!.dataPoints.size, equalTo(2))

        assertThat(mActivityRule.activity.heatmapPainters.size, equalTo(1))
        //Reset default repo
        HeatmapRepository.daoProvider = { MockHeatmapDao() }
    }

    @Test
    fun canUpdateUserLocation() {
        //TODO Rewrite this test
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
    }

    @Test
    fun canUpdateUserLocationTwice() {
        //TODO Rewrite this test
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
        CentralLocationManager.currentUserPosition.postValue(FAKE_LOCATION_TEST)
    }

    @Test
    fun canOnRequestPermissionResult() {
        //TODO Rewrite this test
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mActivityRule.activity.onRequestPermissionsResult(1011, Array(0) { "" }, IntArray(0))
    }

    @Test
    fun droneStatusIsVisibleForoperator() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        onView(withId(R.id.drone_status)).check(matches(isDisplayed()))
    }

    @Test
    fun longClickOnMapAddAMarker() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
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
    fun clickOnMapInteractWithQuadrilateralSearchArea() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        runOnUiThread {
            mActivityRule.activity.setStrategy(SimpleQuadStrategy(Drone.GROUND_SENSOR_SCOPE))
        }
        val searchAreaBuilder = mActivityRule.activity.searchAreaBuilder

        // Add a point
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
            mActivityRule.activity.onMapClicked(LatLng(1.0, 0.0))
        }

        assertThat(searchAreaBuilder.vertices.size, equalTo(2))
        runOnUiThread {
            searchAreaBuilder.reset()
        }
        assertThat(searchAreaBuilder.vertices.size, equalTo(0))
    }

    @Test
    fun clickOnMapInteractWithCircleSearchArea() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        runOnUiThread {
            mActivityRule.activity.setStrategy(SpiralStrategy(Drone.GROUND_SENSOR_SCOPE))
        }
        val searchAreaBuilder = mActivityRule.activity.searchAreaBuilder

        // Add a point
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
            mActivityRule.activity.onMapClicked(LatLng(0.0001, 0.0))
        }

        assertThat(searchAreaBuilder.vertices.size, equalTo(2))
        runOnUiThread {
            searchAreaBuilder.reset()
        }
        assertThat(searchAreaBuilder.vertices.size, equalTo(0))
    }

    @Test
    fun whenExceptionAppendInSearchAreaBuilderAToastIsDisplayed() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        // Add 5 points
        runOnUiThread {
            mActivityRule.activity.onMapClicked(LatLng(0.0, 0.0))
            mActivityRule.activity.onMapClicked(LatLng(0.00001, 0.00001))
            mActivityRule.activity.onMapClicked(LatLng(0.00002, 0.00002))
            mActivityRule.activity.onMapClicked(LatLng(0.00003, 0.00003))
            mActivityRule.activity.onMapClicked(LatLng(0.00004, 0.00004))
        }

        onView(withText("Already enough points"))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun deleteButtonRemovesWaypoints() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
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
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))

        onView(withId(R.id.floating_menu_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())
        onView(withId(R.id.store_button)).perform(click())

        intended(hasComponent(OfflineManagerActivity::class.java.name))
    }

    @Test
    fun locateButtonIsWorking() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
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
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = null
        }
        onView(withId(R.id.battery_level)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 1F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 100%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 0F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 0%")))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = 0.5F
        }
        onView(withId(R.id.battery_level)).check(matches(withText(" 50%")))
    }

    @Test
    fun updateDroneAltitudeChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = null
        }

        onView(withId(R.id.altitude)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 0F
        }
        onView(withId(R.id.altitude)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 1.123F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 1.1 m")))

        runOnUiThread {
            Drone.currentAbsoluteAltitudeLiveData.value = 10F
        }
        onView(withId(R.id.altitude)).check(matches(withText(" 10.0 m")))
    }

    @Test
    fun updateDroneSpeedChangesDroneStatus() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)

        runOnUiThread {
            Drone.currentSpeedLiveData.value = null
        }

        onView(withId(R.id.speed)).check(matches(withText(R.string.no_info)))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 0F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 0.0 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 1.123F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 1.1 m/s")))

        runOnUiThread {
            Drone.currentSpeedLiveData.value = 5.2F
        }
        onView(withId(R.id.speed)).check(matches(withText(" 5.2 m/s")))
    }

    @Test
    fun updateDronePositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        runOnUiThread {
            CentralLocationManager.currentUserPosition.value = LatLng(0.0, 0.0)
            Drone.currentPositionLiveData.value = LatLng(0.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(withText(DEFAULT_ALTITUDE_DISPLAY)))

        runOnUiThread {
            Drone.currentPositionLiveData.value = LatLng(1.0, 0.0)
        }
        onView(withId(R.id.distance_to_user)).check(matches(not(withText(DEFAULT_ALTITUDE_DISPLAY))))
    }

    @Test
    fun updateUserPositionChangesDistToUser() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        runOnUiThread {
            Drone.currentPositionLiveData.value = LatLng(0.0, 0.0)
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
            Drone.currentBatteryLevelLiveData.value = .00f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery1))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .10f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery2))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .30f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery3))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .50f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery4))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .70f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery5))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .90f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery6))))

        runOnUiThread {
            Drone.currentBatteryLevelLiveData.value = .98f
        }
        onView(withId(R.id.battery_level_icon)).check(matches(withTagValue(equalTo(R.drawable.ic_battery7))))
    }

    @Test
    fun resizeButtonIsWorking() {
        mActivityRule.launchActivity(intentWithGroupAndOperator)
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.isCameraFragmentFullScreen, `is`(false))
        onView(withId(R.id.resize_button)).perform(click())
        assertThat(mActivityRule.activity.isCameraFragmentFullScreen, `is`(true))
        onView(withId(R.id.resize_button)).perform(click())
        assertThat(mActivityRule.activity.isCameraFragmentFullScreen, `is`(false))
    }
}