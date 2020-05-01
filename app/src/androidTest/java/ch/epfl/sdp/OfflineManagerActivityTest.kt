package ch.epfl.sdp

import android.widget.ProgressBar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.ui.maps.MapUtils.getCameraWithParameters
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineManagerActivity
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.deleteOfflineRegion
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineRegionUtils.getRegionName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private lateinit var mUiDevice: UiDevice
    private lateinit var offlineManager: OfflineManager

    companion object {
        private const val ZOOM = 10.0

        private val FAKE_MAP_LOCATION_1 = LatLng(46.399111, -31.697953)
        private const val FAKE_MAP_NAME_1 = "RandomName"
        private val FAKE_MAP_LOCATION_2: LatLng = LatLng(42.125, -30.229)
        private const val FAKE_MAP_NAME_2 = "SEA"

        private const val MAP_LOADING_TIMEOUT: Long = 2000
        private const val EPSILON = 1e-3
        private const val POSITIVE_BUTTON_ID: Int = android.R.id.button1
        private const val NEGATIVE_BUTTON_ID: Int = android.R.id.button2
        private const val NEUTRAL_BUTTON_ID: Int = android.R.id.button3
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(OfflineManagerActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        offlineManager = OfflineManager.getInstance(applicationContext())

        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))
        moveCameraToPosition(LatLng(0.0, 0.0))
    }

    private fun moveCameraToPosition(pos: LatLng) {
        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = getCameraWithParameters(pos, ZOOM)
            }
        }
    }

    @Test
    fun checkToastWhenNoMapsHaveBeenDownloaded() {
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                if (offlineRegions.isNotEmpty()) {
                    for (or in offlineRegions) {
                        deleteOfflineRegion(or, ProgressBar(applicationContext()), mActivityRule.activity.mapView)
                    }
                }
            }

            override fun onError(error: String) {} //left intentionally empty
        })
        onView(withId(R.id.list_button)).perform(click())
        onView(withText(applicationContext().getString(R.string.toast_no_regions_yet)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    /**
     * this also tests that Toast are shown
     */
    @Test
    fun canDownloadAndThenDeleteMap() {
        //check that the downloaded list map is empty
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }

            override fun onError(error: String) {} //left intentionally empty
        })

        //DOWNLOAD part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(FAKE_MAP_NAME_1))
        mUiDevice.pressBack()

        onView(withId(POSITIVE_BUTTON_ID)).perform(click())

        onView(withText(applicationContext().getString(R.string.end_progress_success)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                //check that the region has been downloaded
                assertThat(FAKE_MAP_NAME_1, equalTo(getRegionName(offlineRegions[0])))
            }

            override fun onError(error: String) {} //left intentionally empty
        })

        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEGATIVE_BUTTON_ID)).perform(click())

        //DELETE PART
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEUTRAL_BUTTON_ID)).perform(click())
        onView(withText(applicationContext().getString(R.string.toast_region_deleted)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        //check that the downloaded list map is empty
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }

            override fun onError(error: String) {} //left intentionally empty
        })
    }

    /**
     * We move the camera over CMA
     * Download CMA map
     * Then we move the camera somewhere random on the globe
     * And finally we try to navigate back to CMA
     *
     * this also tests that Toast are shown
     */
    @Test
    fun canNavigateToDownloadedMap() {
        val randomLocation = LatLng((-90..90).random().toDouble(), (-180..180).random().toDouble())

        moveCameraToPosition(FAKE_MAP_LOCATION_2)

        //check that the downloaded list map is empty
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }

            override fun onError(error: String) {} //left intentionally empty
        })

        //DOWNLOAD Part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(FAKE_MAP_NAME_2))
        mUiDevice.pressBack()

        onView(withId(POSITIVE_BUTTON_ID)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT * 15)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        onView(withText(applicationContext().getString(R.string.end_progress_success)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                //check that the region has been downloaded
                assertThat(getRegionName(offlineRegions[0]), equalTo(FAKE_MAP_NAME_2))
            }

            override fun onError(error: String) {} //left intentionally empty
        })

        runOnUiThread {
            moveCameraToPosition(randomLocation)
        }

        //NAVIGATE Part
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(FAKE_MAP_NAME_2))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), `is`(true))

        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.latitude, closeTo(FAKE_MAP_LOCATION_2.latitude, EPSILON))
                assertThat(mapboxMap.cameraPosition.target.longitude, closeTo(FAKE_MAP_LOCATION_2.longitude, EPSILON))
            }
        }

        //DELETE PART
        onView(withId(R.id.list_button)).perform(click())
        onView(withId(NEUTRAL_BUTTON_ID)).perform(click())

        onView(withText(applicationContext().getString(R.string.toast_region_deleted)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))

        //check that the downloaded list map is empty
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                assert(offlineRegions.isEmpty())
            }

            override fun onError(error: String) {} //left intentionally empty
        })
    }

    @Test
    fun canClickOnCancelDownloadDialog() {
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(NEGATIVE_BUTTON_ID)).perform(click())
    }

    @Test
    fun checkToastWhenMapHasEmptyName() {
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        onView(withText(applicationContext().getString(R.string.dialog_toast)))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }
}