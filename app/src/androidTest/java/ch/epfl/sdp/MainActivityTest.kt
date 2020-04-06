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
import androidx.test.espresso.intent.matcher.IntentMatchers.filterEquals
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private var mUiDevice: UiDevice? = null

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION)

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

    @Test
    fun canOpenSettings() {
        openActionBarOverflowOrOptionsMenu(getContext())
        onView(withText("Settings")).perform(click())
        intended(hasComponent(SettingsActivity::class.qualifiedName))
    }

    private fun openDrawer() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())
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

        var longitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_longitude), null)
        var latitude: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_latitude), null)
        var zoom: String? = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_zoom), null)
        assertThat(latitude, `is`(nullValue()))
        assertThat(longitude, `is`(nullValue()))
        assertThat(zoom, `is`(nullValue()))

        // Trigger saving mechanism by opening map and coming back
        onView(withId(R.id.display_map)).perform(click())
        mUiDevice?.pressBack()

        longitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_longitude), null)
        latitude = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_latitude), null)
        zoom = sharedPreferences.getString(mActivityRule.activity.getString(R.string.prefs_zoom), null)
        assertThat(latitude, `is`(notNullValue()))
        assertThat(longitude, `is`(notNullValue()))
        assertThat(zoom, `is`(notNullValue()))
    }

    private fun getGSO(): GoogleSignInOptions {
        return GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272117878019-uf5rlbbkl6vhvkkmin8cumueil5ummfs.apps.googleusercontent.com")
                .requestEmail()
                .build()
    }

    @Test
    fun clickOnUserProfilePictureOpensLoginMenu() {
        openDrawer()
        onView(withId(R.id.nav_user_image)).perform(click())

        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
        intended(filterEquals(mGoogleSignInClient.signInIntent))

        // Leave the external popup
        mUiDevice?.pressBack()
    }

    @Test
    fun clickOnUserEmailOpensLoginMenu() {
        openDrawer()
        onView(withId(R.id.nav_user_email)).perform(click())

        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
        intended(filterEquals(mGoogleSignInClient.signInIntent))

        // Leave the external popup
        mUiDevice?.pressBack()
    }

    @Test
    fun clickOnUsernameOpensLoginMenu() {
        openDrawer()
        onView(withId(R.id.nav_username)).perform(click())

        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
        intended(filterEquals(mGoogleSignInClient.signInIntent))

        // Leave the external popup
        mUiDevice?.pressBack()
    }

    @Test
    fun updateUserViewUpdatesUserInformationInDrawer() {
        val dummyUserName: String? = "dummy_username"
        val dummyEmail: String? = "dummy_email"
        val dummyURL: String? = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"

        openDrawer()

        runOnUiThread {
            mActivityRule.activity.updateUserView(dummyUserName, dummyEmail, dummyURL)
        }

        onView(withId(R.id.nav_username)).check(matches(withText(dummyUserName)))
        onView(withId(R.id.nav_user_email)).check(matches(withText(dummyEmail)))
    }

    @Test
    fun checkUserViewElementsAreVisible(){
        openDrawer()
        onView(withId(R.id.nav_user_image)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_username)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_user_email)).check(matches(isDisplayed()))
    }

    @Test
    fun updateUserViewWithNullStringsUpdatesUserInformationInDrawer() {
        openDrawer()
        runOnUiThread {
            mActivityRule.activity.updateUserView(null, null, null)
        }
        onView(withId(R.id.nav_username)).check(matches(withText("default_username")))
        onView(withId(R.id.nav_user_email)).check(matches(withText("default_email")))
        onView(withId(R.id.nav_user_image)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingTheHamburgerOpensTheDrawer() {
        runOnUiThread {
            mActivityRule.activity.onSupportNavigateUp()
        }
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
    }

}