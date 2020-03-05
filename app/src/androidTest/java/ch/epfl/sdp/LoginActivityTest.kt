package ch.epfl.sdp

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun testLogin() {
        //Espresso.onView(ViewMatchers.withId(R.id.google_login_btn)).perform(ViewActions.click())
    }
}