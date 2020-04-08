package ch.epfl.sdp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.view.Gravity
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private var mUiDevice: UiDevice? = null

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = grant(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(getInstrumentation())
    }

    private fun getContext(): Context {
        return getInstrumentation().targetContext
    }

    private fun openDrawer() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())
    }

    @Test
    fun canOpenSettings() {
        openActionBarOverflowOrOptionsMenu(getContext())
        onView(withText("Settings")).perform(click())
        intended(hasComponent(SettingsActivity::class.qualifiedName))
    }

    @Test
    fun canNavigateToHome() {
        openDrawer()
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_home))
    }

    @Test
    fun canNavigateToMapsManaging() {
        openDrawer()
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing))
    }

    @Test
    fun canDisplayTheVideo() {
        onView(withId(R.id.display_camera)).perform(click())
        getInstrumentation().waitForIdleSync()
        mUiDevice?.pressBack()
    }

    @Test
    fun canDisplayAMapAndReloadLocation() {
        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_latitude), null) == null)
        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_longitude), null) == null)
        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_zoom), null) == null)

        onView(withId(R.id.display_map)).perform(click())
        getInstrumentation().waitForIdleSync()
        mUiDevice?.pressBack()

        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_latitude), null) != null)
        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_longitude), null) != null)
        assert(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(mActivityRule.activity.getString(R.string.prefs_zoom), null) != null)

        //Return on the view as to load the preferences this time
        getInstrumentation().waitForIdleSync()
        onView(withId(R.id.display_map)).perform(click())
    }

    @Test
    fun clickingTheHamburgerOpensTheDrawer() {
        runOnUiThread {
            mActivityRule.activity.onSupportNavigateUp()
        }
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        mUiDevice?.pressBack()
    }

}