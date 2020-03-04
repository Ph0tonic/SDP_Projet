package ch.epfl.sdp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testCanGreetUsers() {
        onView(withId(R.id.mainName)).perform(typeText("from my unit test")).perform(closeSoftKeyboard())
        onView(withId(R.id.mainGoButton)).perform(click())
        onView(withId(R.id.greetingMessage)).check(ViewAssertions.matches(withText("Hello from my unit test!")))
    }

    @Test
    fun testCanOpenSettings(){
        onView(withId(R.id.settingsButton)).perform(click())
    }
}