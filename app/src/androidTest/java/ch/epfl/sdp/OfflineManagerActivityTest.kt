package ch.epfl.sdp

import android.content.Intent

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OfflineManagerActivityTest {

    companion object {
        private const val NAME = "RandomName"
        private lateinit var mUiDevice: UiDevice

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
    }

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mActivityRule.launchActivity(Intent())
        mUiDevice.wait(Until.hasObject(By.desc(MapActivity.MAP_READY_DESCRIPTION)), MapActivityTest.MAP_LOADING_TIMEOUT)
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
    fun canDownloadAndThenDeleteMap(){
        canDownloadMap()

        canNavigateToDownloadedMap()

        canClickOnCancelListDialog()

        canDeleteMap()
    }

    private fun canDownloadMap(){
        clickOnDownloadButton()
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(NAME))
        mUiDevice.pressBack() //hide the keyboard

        clickOnDownloadButton()
        isToastMessageDisplayed(R.string.end_progress_success)
    }

    private fun canNavigateToDownloadedMap(){
        clickOnListButton()
        onView(withText(R.string.navigate_positive_button)).perform(click())
        onView(withText(NAME)).inRoot(ToastMatcher())
                .check(matches(isDisplayed()))
    }

    private fun canClickOnCancelListDialog(){
        clickOnListButton()
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }

    private fun canDeleteMap(){
        clickOnListButton()
        onView(withText(R.string.navigate_neutral_button_title)).perform(click())
        isToastMessageDisplayed(R.string.toast_region_deleted)
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
}

