package ch.epfl.sdp

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginNavFragmentTest {

    private lateinit var mUiDevice: UiDevice

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Auth.logout()
    }

    private fun getGSO(): GoogleSignInOptions {
        return GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(MainApplication.applicationContext().getString(R.string.google_signin_key))
                .requestEmail()
                .build()
    }

    @Test
    fun testEventFragment() {
        onView(withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_signin_button)).perform(click())

        val mGoogleSignInClient = GoogleSignIn.getClient(MainApplication.applicationContext(), getGSO())
        Intents.intended(IntentMatchers.filterEquals(mGoogleSignInClient.signInIntent))
        mUiDevice.pressBack()
    }

//    @Test
//    fun clickOnUserProfilePictureOpensLoginMenu() {
//        Espresso.onView(ViewMatchers.withId(R.id.nav_user_image)).perform(ViewActions.click())
//
//        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
//        Intents.intended(IntentMatchers.filterEquals(mGoogleSignInClient.signInIntent))
//        mUiDevice.pressBack()
//    }

//    @Test
//    fun clickOnUserEmailOpensLoginMenu() {
//        Espresso.onView(ViewMatchers.withId(R.id.nav_user_email)).perform(ViewActions.click())
//
//        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
//        Intents.intended(IntentMatchers.filterEquals(mGoogleSignInClient.signInIntent))
//        mUiDevice.pressBack()
//    }

//    @Test
//    fun clickOnUsernameOpensLoginMenu() {
//        Espresso.onView(ViewMatchers.withId(R.id.nav_signin_button)).perform(ViewActions.click())
//
//        val mGoogleSignInClient = GoogleSignIn.getClient(getContext(), getGSO())
//        Intents.intended(IntentMatchers.filterEquals(mGoogleSignInClient.signInIntent))
//        mUiDevice.pressBack()
//    }

//    @Test
//    fun updateUserViewUpdatesUserInformationInDrawer() {
//        val dummyUserName: String? = "dummy_username"
//        val dummyEmail: String? = "dummy_email"
//        val dummyURL: String? = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
//
//        UiThreadStatement.runOnUiThread {
//            mActivityRule.activity.updateUserView(dummyUserName, dummyEmail, dummyURL)
//        }
//        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
//        Espresso.onView(ViewMatchers.withId(R.id.nav_username)).check(ViewAssertions.matches(ViewMatchers.withText(dummyUserName)))
//        Espresso.onView(ViewMatchers.withId(R.id.nav_user_email)).check(ViewAssertions.matches(ViewMatchers.withText(dummyEmail)))
//    }

//    @Test
//    fun updateUserViewWithNullStringsUpdatesUserInformationInDrawer() {
//        UiThreadStatement.runOnUiThread {
//            mActivityRule.activity.updateUserView(null, null, null)
//        }
//        Espresso.onView(ViewMatchers.withId(R.id.nav_username)).check(ViewAssertions.matches(ViewMatchers.withText("default_username")))
//        Espresso.onView(ViewMatchers.withId(R.id.nav_user_email)).check(ViewAssertions.matches(ViewMatchers.withText("default_email")))
//        Espresso.onView(ViewMatchers.withId(R.id.nav_user_image)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//    }

}