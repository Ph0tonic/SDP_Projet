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
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private var mUiDevice: UiDevice? = null

    @get:Rule
    val mActivityRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun canOpenSettings(){
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText("Settings")).perform(click())
        intended(hasComponent(SettingsActivity::class.qualifiedName))
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
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272117878019-uf5rlbbkl6vhvkkmin8cumueil5ummfs.apps.googleusercontent.com")
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(InstrumentationRegistry.getInstrumentation().targetContext, gso)
        intended(allOf(
                hasAction(mGoogleSignInClient.signInIntent.action),
                hasComponent(mGoogleSignInClient.signInIntent.component),
                hasPackage(mGoogleSignInClient.signInIntent.`package`)))
        //intended(allOf(hasAction("com.google.android.gms.auth.GOOGLE_SIGN_IN"), hasPackage("ch.epfl.sdp")))
        mUiDevice?.pressBack()
    }

    @Test
    fun clickOnUserEmailOpensLoginMenu(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_user_email)).perform(click())
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272117878019-uf5rlbbkl6vhvkkmin8cumueil5ummfs.apps.googleusercontent.com")
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(InstrumentationRegistry.getInstrumentation().targetContext, gso)
        //intended(hasAction(""))
        intended(allOf(
                hasAction(mGoogleSignInClient.signInIntent.action),
                hasComponent(mGoogleSignInClient.signInIntent.component),
                hasPackage(mGoogleSignInClient.signInIntent.`package`)))
        //intended(allOf(hasAction("com.google.android.gms.auth.GOOGLE_SIGN_IN"), hasPackage("ch.epfl.sdp")))
        mUiDevice?.pressBack()
    }

    @Test
    fun clickOnUsernameOpensLoginMenu(){
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_username)).perform(click())
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272117878019-uf5rlbbkl6vhvkkmin8cumueil5ummfs.apps.googleusercontent.com")
                .requestEmail()
                .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(InstrumentationRegistry.getInstrumentation().targetContext, gso)
        intended(allOf(
                hasAction(mGoogleSignInClient.signInIntent.action),
                hasComponent(mGoogleSignInClient.signInIntent.component),
                hasPackage(mGoogleSignInClient.signInIntent.`package`)))
        //intended(allOf(hasAction("com.google.android.gms.auth.GOOGLE_SIGN_IN"), hasPackage("ch.epfl.sdp")))
        mUiDevice?.pressBack()
    }

    @Test
    fun updateUserViewUpdatesUserInformationInDrawer(){
        val dummyUserName = "dummy_username"
        val dummyEmail = "dummy_email"

        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Drawer is closed to begin with
                .perform(DrawerActions.open())

        runOnUiThread{
            mActivityRule.activity.updateUserView(dummyUserName, dummyEmail)
        }
        onView(withId(R.id.nav_username)).check(matches(withText(dummyUserName)))
        onView(withId(R.id.nav_user_email)).check(matches(withText(dummyEmail)))
    }

    @Test
    fun clickingTheHamburgerOpensTheDrawer(){
        mActivityRule.activity.onSupportNavigateUp()
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
    }
}