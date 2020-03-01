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
class MainActivityTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testCanGreetUsers() {
        Espresso.onView(ViewMatchers.withId(R.id.mainName)).perform(ViewActions.typeText("from my unit test")).perform(ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.mainGoButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.greetingMessage)).check(ViewAssertions.matches(ViewMatchers.withText("Hello from my unit test!")))
    }
}