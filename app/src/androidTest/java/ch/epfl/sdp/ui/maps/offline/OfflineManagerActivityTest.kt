package ch.epfl.sdp.ui.maps.offline

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import ch.epfl.sdp.R
import ch.epfl.sdp.drone.DroneInstanceMock
import ch.epfl.sdp.map.MapUtils.getCameraWithParameters
import ch.epfl.sdp.map.offline.OfflineRegionUtils.getRegionName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private lateinit var mUiDevice: UiDevice
    private lateinit var offlineManager: OfflineManager

    companion object {
        private const val ZOOM_VALUE = 15.0

        private const val FAKE_MAP_NAME = "Random_Name"
        private val FAKE_MAP_LOCATION = LatLng(14.0, 12.0)

        private const val MAP_LOADING_TIMEOUT = 30000L
        private const val POSITIVE_BUTTON_ID: Int = android.R.id.button1
        private const val NEGATIVE_BUTTON_ID: Int = android.R.id.button2

        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(OfflineManagerActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        DroneInstanceMock.setupDefaultMocks()
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        offlineManager = OfflineManager.getInstance(applicationContext())

        mUiDevice.wait(Until.hasObject(By.desc(applicationContext().getString(R.string.map_ready))), MAP_LOADING_TIMEOUT)
        assertThat(mActivityRule.activity.mapView.contentDescription == applicationContext().getString(R.string.map_ready), equalTo(true))
        moveCameraToPosition(FAKE_MAP_LOCATION)
    }

    private fun moveCameraToPosition(pos: LatLng) {
        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = getCameraWithParameters(pos, ZOOM_VALUE)
            }
        }
    }

    @Test
    fun canDownloadMap() {
        //Check that the downloaded list map is empty
        val checkedIfEmpty = CountDownLatch(1)
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                val emptiedLatch = CountDownLatch(offlineRegions.size)
                offlineRegions.forEach {
                    it.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() {
                            emptiedLatch.countDown()
                        }

                        override fun onError(error: String?) {}
                    })
                }
                emptiedLatch.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
                assertThat(emptiedLatch.count, equalTo(0L))

                checkedIfEmpty.countDown()
            }

            override fun onError(error: String) {} //left intentionally empty
        })
        checkedIfEmpty.await(ASYNC_CALL_TIMEOUT * 2, TimeUnit.SECONDS)
        assertThat(checkedIfEmpty.count, equalTo(0L))

        //DOWNLOAD part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(FAKE_MAP_NAME))
        mUiDevice.pressBack()

        onView(withId(POSITIVE_BUTTON_ID)).perform(click())

        val calledList = CountDownLatch(1)
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                //check that the region has been downloaded
                assertThat(getRegionName(offlineRegions[0]), equalTo(FAKE_MAP_NAME))
                calledList.countDown()
            }

            override fun onError(error: String) {} //left intentionally empty
        })

        calledList.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(calledList.count, equalTo(0L))

        //DELETE PART
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                offlineRegions.forEach { it ->
                    it.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() {}
                        override fun onError(error: String?) {}
                    })
                }
            }

            override fun onError(error: String?) {}
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