package ch.epfl.sdp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MapActivity.Companion.MAP_READY_DESCRIPTION
import ch.epfl.sdp.MapActivityTest.Companion.MAP_LOADING_TIMEOUT
import ch.epfl.sdp.ui.maps.MapUtils.getCameraWithParameters
import ch.epfl.sdp.ui.offlineMapsManaging.OfflineManagerActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private lateinit var mUiDevice: UiDevice

    companion object {
        private const val RANDOM_NAME = "RandomName"
        private const val CMA_NAME = "CMA"
        private val CMA: LatLng = LatLng(46.317261, 7.485201)
        private const val EPSILON = 1e-3
        private const val POSITIVE_BUTTON_ID: Int = android.R.id.button1
        private const val NEGATIVE_BUTTON_ID: Int = android.R.id.button2
        private const val NEUTRAL_BUTTON_ID: Int = android.R.id.button3

        private fun clickOnDownloadButton() {
            onView(withId(R.id.download_button)).perform(click())
        }

        private fun clickOnDownloadButtonInDialog() {
            onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        }

        private fun clickOnListButton() {
            onView(withId(R.id.list_button)).perform(click())
        }

        private fun clickOnCancelInListDialog() {
            clickOnListButton()
            onView(withId(NEGATIVE_BUTTON_ID)).perform(click())
        }

    }

    @get:Rule
    var mActivityRule = IntentsTestRule(OfflineManagerActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        moveCameraToPosition(LatLng(0.0, 0.0))
        UiThreadStatement.runOnUiThread {
            mUiDevice.wait(Until.hasObject(By.desc(MAP_READY_DESCRIPTION)), MAP_LOADING_TIMEOUT)
        }
    }

    private fun checkToastMessageIsDisplayed(message: String) {
        onView(withText(message))
                .inRoot(withDecorView(not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }

    private fun deleteMap() {
        clickOnListButton()
        onView(withId(NEUTRAL_BUTTON_ID)).perform(click())
        checkToastMessageIsDisplayed(MainApplication.applicationContext().getString(R.string.toast_region_deleted))
    }

    private fun downloadMap(name: String) {
        clickOnDownloadButton()
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(name))
        mUiDevice.pressBack() //hide the keyboard

        clickOnDownloadButtonInDialog()
        checkToastMessageIsDisplayed(MainApplication.applicationContext().getString(R.string.end_progress_success))
    }

    private fun moveCameraToPosition(pos: LatLng) {
        UiThreadStatement.runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = getCameraWithParameters(pos, 15.0)
                mapboxMap.cameraPosition = getCameraWithParameters(pos, 15.0)
            }
        }
    }

    private fun navigateToDownloadedMap(name: String) {
        clickOnListButton()
        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        checkToastMessageIsDisplayed(name)
    }

    @Test
    fun cannotClickOnListWhenNoDownloadedMap() {
        clickOnListButton()
        checkToastMessageIsDisplayed(MainApplication.applicationContext().getString(R.string.toast_no_regions_yet))
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

        navigateToDownloadedMap(CMA_NAME)

        mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
            assertThat(mapboxMap.cameraPosition.target.latitude, Matchers.closeTo(CMA.latitude, EPSILON))
            assertThat(mapboxMap.cameraPosition.target.longitude, Matchers.closeTo(CMA.longitude, EPSILON))
        }

        deleteMap()
    }

    @Test
    fun canClickOnCancelDownloadDialog() {
        clickOnDownloadButton()
        onView(withId(NEGATIVE_BUTTON_ID)).perform(click())
    }

    @Test
    fun cannotDownloadEmptyMapName() {
        clickOnDownloadButton()
        clickOnDownloadButtonInDialog()
        checkToastMessageIsDisplayed(MainApplication.applicationContext().getString(R.string.dialog_toast))
    }
}

