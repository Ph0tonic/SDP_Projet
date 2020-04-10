package ch.epfl.sdp

import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
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
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
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
        private const val NAME = "RandomName"
    }

    private lateinit var mUiDevice: UiDevice


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
    fun a_cannotListWhenNoMap() {
        clickOnListButton()
        isToastMessageDisplayed(R.string.toast_no_regions_yet)
    }

    @Test
    fun b_canDownloadMap() {
        clickOnDownloadButton()
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(NAME))

        mUiDevice.pressBack() //hide the keyboard

        clickOnDownloadButton()
    }

    @Test
    fun canCancelDownload() {
        clickOnDownloadButton()
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }

    @Test
    fun canCancelList() {
        clickOnListButton()
        onView(withText(R.string.dialog_negative_button)).perform(click())
    }

    @Test
    fun canNavigateTo() {
        clickOnListButton()
        onView(withText(R.string.navigate_positive_button)).perform(click())
    }

    @Test
    fun canOpenDownloadDialog() {
        clickOnDownloadButton()
        clickOnDownloadButton() //normal duplicate
        onView(withId(R.id.dialog_textfield_id)).check(matches(isDisplayed()))
    }

    @Test
    fun canOpenListDialog() {
        clickOnListButton()
        onView(withText(R.string.navigate_title)).check(matches(isDisplayed()))
    }




    @Test
    fun cannotEmptyDownloadName() {
        clickOnDownloadButton()
        isToastMessageDisplayed(R.string.dialog_toast)
    }

    @Test
    fun z_canDeleteMap() {
        clickOnListButton()
        onView(withText(R.string.navigate_neutral_button_title)).perform(click())
    }

    private fun isToastMessageDisplayed(textId: Int) {
        onView(withText(textId)).inRoot(ToastMatcher())
                .check(matches(isDisplayed()))
    }

    private fun clickOnDownloadButton() {
        onView(withText(R.string.dialog_positive_button)).perform(click())
    }

    private fun clickOnListButton() {
        onView(withText(R.string.navigate_title)).perform(click())
    }
}

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    @Suppress("DEPRECATION")
    public override fun matchesSafely(root: Root): Boolean {
        val type: Int = root.windowLayoutParams.get().type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken: IBinder = root.decorView.windowToken
            val appToken: IBinder = root.decorView.applicationWindowToken
            if (windowToken === appToken) { // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true
            }
        }
        return false
    }
}
