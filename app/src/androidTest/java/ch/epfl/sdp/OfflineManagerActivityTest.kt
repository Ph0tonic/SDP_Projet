package ch.epfl.sdp

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MapActivity.Companion.MAP_READY_DESCRIPTION
import ch.epfl.sdp.MapActivityTest.Companion.MAP_LOADING_TIMEOUT
import ch.epfl.sdp.ui.maps.MapUtils.getCameraWithParameters
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OfflineManagerActivityTest {

    companion object {
        private const val RANDOM_NAME = "RandomName"
        private const val CMA_NAME = "CMA"
        private val CMA: LatLng = LatLng(46.317261, 7.485201)
        private lateinit var mUiDevice: UiDevice
        const val EPSILON = 1e-3


        @get:Rule
        var mActivityRule = ActivityTestRule(
                OfflineManagerActivity::class.java,
                true,
                false) // Activity is not launched immediately

        private fun clickOnDownloadButton() {
            onView(withText(R.string.dialog_positive_button)).perform(click())
        }

        private fun clickOnListButton() {
            onView(withText(R.string.navigate_title)).perform(click())
        }

        private fun isToastMessageDisplayed(textId: Int) {
            onView(withText(textId)).inRoot(ToastMatcher())
                    .check(matches(isDisplayed()))
        }

        private fun downloadMap(name: String) {
            clickOnDownloadButton()
            onView(withId(R.id.dialog_textfield_id)).perform(typeText(name))
            mUiDevice.pressBack() //hide the keyboard

            clickOnDownloadButton()
            isToastMessageDisplayed(R.string.end_progress_success)
        }

        private fun navigateToDownloadedMap(name: String) {
            clickOnListButton()
            onView(withText(R.string.navigate_positive_button)).perform(click())
            onView(withText(name)).inRoot(ToastMatcher())
                    .check(matches(isDisplayed()))
        }

        private fun clickOnCancelInListDialog() {
            clickOnListButton()
            onView(withText(R.string.dialog_negative_button)).perform(click())
        }

        private fun deleteMap() {
            clickOnListButton()
            onView(withText(R.string.navigate_neutral_button_title)).perform(click())
            isToastMessageDisplayed(R.string.toast_region_deleted)
        }
    }

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(MAP_READY_DESCRIPTION)), MAP_LOADING_TIMEOUT)
    }

    @get:Rule
    var mActivityRule = ActivityTestRule(
            OfflineManagerActivity::class.java,
            true,
            false) // Activity is not launched immediately

    @Test
    fun cannotListWhenNoMap() {
        clickOnListButton()
        isToastMessageDisplayed(R.string.toast_no_regions_yet)
    }

    @Test
    fun canDownloadAndThenDeleteMap() {
        downloadMap(RANDOM_NAME)

        navigateToDownloadedMap(RANDOM_NAME)

        clickOnCancelInListDialog()

        deleteMap()
    }

    /**
     * We move the camera over CMA
     * Download CMA map
     * Then we move the camera somewhere random on the globe
     * And finally we try to navigate back to CMA
     */
    @Test
    fun canNavigateToDownloadedMap() {
        val rdmLatLng = LatLng((-90..90).random().toDouble(), (-180..180).random().toDouble())

        moveCameraToPosition(CMA)

        downloadMap(CMA_NAME)

        moveCameraToPosition(rdmLatLng)
        Thread.sleep(2000)

        navigateToDownloadedMap(CMA_NAME)

        UiThreadStatement.runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                assertThat(mapboxMap.cameraPosition.target.latitude, Matchers.closeTo(CMA.latitude, EPSILON))
                assertThat(mapboxMap.cameraPosition.target.longitude, Matchers.closeTo(CMA.longitude, EPSILON))
            }
        }

        deleteMap()
    }

    @Test
    fun canClickOnCancelDownloadDialog() {
        clickOnDownloadButton()
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }




    @Test
    fun cannotEmptyDownloadName() {
        clickOnDownloadButton()
        clickOnDownloadButton()
        isToastMessageDisplayed(R.string.dialog_toast)
    }

    private fun moveCameraToPosition(pos: LatLng) {
        UiThreadStatement.runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = getCameraWithParameters(pos, 15.0)
            }
        }
    }
}

