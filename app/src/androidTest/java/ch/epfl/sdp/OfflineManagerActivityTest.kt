package ch.epfl.sdp

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.activity_map.*
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OfflineManagerActivityTest {
    private var mUiDevice: UiDevice? = null

    private val cmalatitude = 46.307165438
    private val cmalongitude = 7.476331
    private val zoom = 10.0
    private val name = "Crans-Montana"

    private val oceanlatitude =  46.399111
    private val oceanlongitude = -31.697953
    private val oceanName = "Pacifique"
    private val oceanZoom = 19.0
    private val timeout : Long = 2000

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        runOnUiThread {
            mActivityRule.activity.mapView.getMapAsync { mapboxMap ->
                mapboxMap.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(oceanlatitude, oceanlongitude))
                        .zoom(oceanZoom)
                        .build()
            }
        }
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
    fun canOpenDownloadDialog(){
        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD").clickable(true)), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("Enter")), timeout)
        onView(withId(R.id.dialog_textfield_id))
               .check(matches(isDisplayed()))
    }

    @Test
    fun canOpenListDialog(){
        mUiDevice?.wait(Until.hasObject(By.desc("List")), timeout)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("List")), timeout)
        onView(withText(R.string.navigate_title))
                .check(matches(isDisplayed()))
    }

    @Test
    fun canCancelDownload(){
        mUiDevice?.wait(Until.hasObject(By.desc("Download")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("CANCEL")), timeout)
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }

    @Test
    fun canCancelList(){
        mUiDevice?.wait(Until.hasObject(By.desc("List").clickable(true)), timeout)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("CANCEL").clickable(true)), timeout)
        onView(withText(R.string.navigate_negative_button_title)).perform(click())
    }

    @Test
    fun canDownloadMap(){
        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("Enter")), timeout)
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(name))

        mUiDevice?.pressBack()

        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())
    }

    @Test
    fun canDeleteOceanMap(){

        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("Enter")), timeout)
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(oceanName))

        mUiDevice?.pressBack()

        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        Thread.sleep(timeout*30)

        mUiDevice?.wait(Until.hasObject(By.desc("List")),  timeout)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("DELETE")), timeout)
        onView(withText(R.string.navigate_neutral_button_title)).perform(click())
    }

    /*
    @Test
    fun canDeleteMap(){

        mUiDevice?.wait(Until.hasObject(By.desc("MAP READY")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("Enter")), timeout)
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(name))

        mUiDevice?.pressBack()

        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD").clickable(true)), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("List").clickable(true)),  timeout * 300)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("DELETE").clickable(true)), timeout)
        onView(withText(R.string.navigate_neutral_button_title)).perform(click())
    }
     */

    @Test
    fun canNavigateTo(){
        mUiDevice?.wait(Until.hasObject(By.desc("List")), timeout)
        onView(withId(R.id.list_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("NAVIGATE")), timeout)
        onView(withText(R.string.navigate_positive_button)).perform(click())
    }

    @Test
    fun cannotEmptyDownloadName(){
        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())

        mUiDevice?.wait(Until.hasObject(By.desc("DOWNLOAD")), timeout)
        onView(withText(R.string.dialog_positive_button)).perform(click())
    }
}