package ch.epfl.sdp

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.R.string.dialog_title
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.activity_map.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private var mUiDevice: UiDevice? = null
    private val latitude = 46.307165438
    private val longitude = 7.476331
    private val zoom = 10.0
    private val name = "Crans-Montana"

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }


    @get:Rule
    var mActivityRule = ActivityTestRule(
            OfflineManagerActivity::class.java,
            true,
            true) // Activity is not launched immediately

    private fun getContext(): Context{
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testCheckDownloadDialogDisplayed(){
        onView(withText(R.string.dialog_positive_button)).perform(click())
        onView(withId(R.integer.dialog_textfield_id))
               .check(matches(isDisplayed()))
    }

    @Test
    fun testCheckListDialogDisplayed(){
        onView(withText(R.string.navigate_title)).perform(click())
        onView(withText(R.string.navigate_title))
                .check(matches(isDisplayed()))
    }

    @Test
    fun canDownloadMap(){
        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(latitude, longitude))
                        .zoom(zoom)
                        .build()
            }
            onView(withText(R.string.dialog_positive_button)).perform(click())
            onView(withId(R.integer.dialog_textfield_id)).perform(typeText(name))
            mUiDevice?.pressBack()
            onView(withText(R.string.dialog_positive_button)).perform(click())
        }
    }

}