package ch.epfl.sdp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
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
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var mUiDevice: UiDevice
    private val intentWithGroup = Intent().putExtra(GROUP_ID_PROPERTY_NAME_FOR_INTENT, DUMMY_GROUP_ID)

    companion object {
        const val GROUP_ID_PROPERTY_NAME_FOR_INTENT = "groupId"
        const val DUMMY_GROUP_ID = "DummyGroupId"
    }

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = grant(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java, true, false)

    @Before
    @Throws(Exception::class)
    fun before() {
        mActivityRule.launchActivity(intentWithGroup)
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
        onView(withId(R.id.video_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun canDisplayAMapAndReloadLocation() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext())

        sharedPreferences.edit().clear().apply()

        //Fake logged in
        runOnUiThread {
            Auth.accountId.value = MapActivityTest.FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        var longitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_longitude), null)
        var latitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_latitude), null)
        var zoom: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_zoom), null)
        assertThat(latitude, `is`(nullValue()))
        assertThat(longitude, `is`(nullValue()))
        assertThat(zoom, `is`(nullValue()))

        // Trigger saving mechanism by opening map and coming back
        onView(withId(R.id.display_map)).perform(click())
        mUiDevice.pressBack()

        longitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_longitude), null)
        latitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_latitude), null)
        zoom = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_zoom), null)
        assertThat(latitude, `is`(notNullValue()))
        assertThat(longitude, `is`(notNullValue()))
        assertThat(zoom, `is`(notNullValue()))
    }

    @Test
    fun clickingTheHamburgerOpensTheDrawer() {
        runOnUiThread {
            mActivityRule.activity.onSupportNavigateUp()
        }
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        mUiDevice.pressBack()
    }
}