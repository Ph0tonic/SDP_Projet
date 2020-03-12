package ch.epfl.sdp

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
        Espresso.onView(withId(R.id.google_login_btn)).perform(click())
        Thread.sleep(5000);
        println("DOne sleeping")
        //Espresso.pressBack()
        //Espresso.onView(withText("None of the above")).perform(click());
    }
}