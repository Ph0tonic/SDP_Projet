package ch.epfl.sdp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.ui.maps.MapUtils
import com.mapbox.mapboxsdk.geometry.LatLng
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
        private const val ZOOM = 10.0
        private const val NAME = "Crans-Montana"

        private const val OCEAN_LATITUDE = 46.399111
        private const val OCEAN_LONGITUDE = -31.697953
        private const val TIMEOUT: Long = 2000
    }

    private lateinit var mUiDevice: UiDevice


    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = MapUtils.getCameraWithParameters(LatLng(OCEAN_LATITUDE, OCEAN_LONGITUDE), ZOOM)
            }
        }
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(OfflineManagerActivity::class.java)

    @Test
    fun canOpenDownloadDialog() {
        mUiDevice.wait(Until.hasObject(By.desc("DOWNLOAD").clickable(true)), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("Enter")), TIMEOUT)
        onView(withId(R.id.dialog_textfield_id)).check(matches(isDisplayed()))
    }

    @Test
    fun canOpenListDialog() {
        mUiDevice.wait(Until.hasObject(By.desc("List")), TIMEOUT)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("List")), TIMEOUT)
        onView(withText(R.string.navigate_title)).check(matches(isDisplayed()))
    }

    @Test
    fun canCancelDownload() {
        mUiDevice.wait(Until.hasObject(By.desc("Download")), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("CANCEL")), TIMEOUT)
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }

    @Test
    fun canCancelList() {
        mUiDevice.wait(Until.hasObject(By.desc("List").clickable(true)), TIMEOUT)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("CANCEL").clickable(true)), TIMEOUT)
        onView(withText(R.string.navigate_negative_button_title)).perform(click())
    }

    @Test
    fun a_canDownloadMap() {
        mUiDevice.wait(Until.hasObject(By.desc(MapActivity.MAP_READY_DESCRIPTION)), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("Enter")), TIMEOUT)
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(NAME))

        mUiDevice.pressBack()

        mUiDevice.wait(Until.hasObject(By.desc("DOWNLOAD")), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())
    }

    @Test
    fun z_canDeleteOceanMap() {

        mUiDevice.wait(Until.hasObject(By.desc("List")), TIMEOUT)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("DELETE")), TIMEOUT)
        onView(withText(R.string.navigate_neutral_button_title)).perform(click())
    }

    @Test
    fun canNavigateTo() {
        mUiDevice.wait(Until.hasObject(By.desc("List")), TIMEOUT)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("NAVIGATE")), TIMEOUT)
        onView(withText(R.string.navigate_positive_button)).perform(click())
    }

    @Test
    fun cannotEmptyDownloadName() {
        mUiDevice.wait(Until.hasObject(By.desc("DOWNLOAD")), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc("DOWNLOAD")), TIMEOUT)
        onView(withText(R.string.dialog_positive_button)).perform(click())
    }
}