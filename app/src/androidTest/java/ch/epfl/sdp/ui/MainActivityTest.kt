package ch.epfl.sdp.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.view.Gravity
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.MockGroupDao
import ch.epfl.sdp.database.dao.MockUserDao
import ch.epfl.sdp.database.dao.OfflineHeatmapDao
import ch.epfl.sdp.database.dao.OfflineMarkerDao
import ch.epfl.sdp.database.data_manager.MainDataManager
import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.MarkerRepository
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.drone.DroneInstanceMock
import ch.epfl.sdp.ui.settings.SettingsActivity
import ch.epfl.sdp.utils.Auth
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var mUiDevice: UiDevice

    companion object {
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
    }

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = grant(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        DroneInstanceMock.setupDefaultMocks()

        HeatmapRepository.daoProvider = { OfflineHeatmapDao() }
        MarkerRepository.daoProvider = { OfflineMarkerDao() }
        UserRepository.daoProvider = { MockUserDao() }
        SearchGroupRepository.daoProvider = { MockGroupDao() }

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
        onView(withId(R.id.mainSettingsButton)).perform(click())
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

    /*@Test
    fun canDisplayAMapAndReloadLocation() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext())

        sharedPreferences.edit().clear().apply()

        //Fake logged in
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }

        var longitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_longitude), null)
        var latitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_latitude), null)
        var zoom: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_zoom), null)
        assertThat(latitude, `is`(nullValue()))
        assertThat(longitude, `is`(nullValue()))
        assertThat(zoom, `is`(nullValue()))

        // Trigger saving mechanism by opening map and coming back
        onView(withId(R.id.work_offline_button)).perform(click())
        mUiDevice.pressBack()

        longitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_longitude), null)
        latitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_latitude), null)
        zoom = sharedPreferences.getString(mActivityRule.activity.getString(R.string.pref_key_zoom), null)
        assertThat(latitude, `is`(notNullValue()))
        assertThat(longitude, `is`(notNullValue()))
        assertThat(zoom, `is`(notNullValue()))
    }*/

    @Test
    fun clickingTheHamburgerOpensTheDrawer() {
        onView(withId(R.id.mainDrawerButton)).perform(click())
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        mUiDevice.pressBack()
    }

    @Test
    fun startMissionWithNoGroupShowsToast() {
        runOnUiThread {
            MainDataManager.groupId.value = null
        }
        onView(withId(R.id.start_mission_button)).perform(click())
        onView(withText(mActivityRule.activity.getString(R.string.warning_no_group_selected)))
                .inRoot(RootMatchers.withDecorView(Matchers.not(mActivityRule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }
}