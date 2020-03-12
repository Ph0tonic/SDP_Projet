package ch.epfl.sdp

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class Main3ActivityTest {
    private var mUiDevice: UiDevice? = null

    @get:Rule
    val mActivityRule = ActivityTestRule(Main3Activity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun canOpenSettings(){
        Intents.init()
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText("Settings")).perform(click())
        intended(hasComponent(SettingsActivity::class.qualifiedName))
        Intents.release()
    }

    @Test
    fun canNavigateToHome(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_home));
    }

    @Test
    fun canNavigateToMissionDesign(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_misson_design));
    }

    @Test
    fun canNavigateToMapsManaging(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing));
    }

    @Test
    fun clickOnUserProfilePictureOpensLoginMenu(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_user_image)).perform(click())
        val loginScreenTitle = mUiDevice!!.findObject(UiSelector().text("Choose an account"))

        assertThat(loginScreenTitle?.exists(), `is`(true))
        mUiDevice!!.pressBack()
    }

    @Test
    fun clickOnUserEmailOpensLoginMenu(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_user_email)).perform(click())
        val loginScreenTitle = mUiDevice!!.findObject(UiSelector().text("Choose an account"))

        assertThat(loginScreenTitle?.exists(), `is`(true))
        mUiDevice!!.pressBack()
    }

    @Test
    fun clickOnUsernameOpensLoginMenu(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_username)).perform(click())
        val loginScreenTitle = mUiDevice!!.findObject(UiSelector().text("Choose an account"))

        assertThat(loginScreenTitle?.exists(), `is`(true))
        mUiDevice!!.pressBack()
    }
}