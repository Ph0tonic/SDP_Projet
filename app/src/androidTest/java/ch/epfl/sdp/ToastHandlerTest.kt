package ch.epfl.sdp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.widget.Toast
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.ui.toast.ToastHandler
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToastHandlerTest {
    private lateinit var mUiDevice: UiDevice

    companion object {
        private const val TOAST_TEXT_ID = R.string.drone_user_error
    }

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

    @Test
    fun canShowToastFromString() {
        runOnUiThread {
            ToastHandler().showToast(TOAST_TEXT_ID, Toast.LENGTH_SHORT)
        }

        // Test that the toast is displayed
        Espresso.onView(ViewMatchers.withText(MainApplication.applicationContext().getString(TOAST_TEXT_ID)))
                .inRoot(RootMatchers.withDecorView(CoreMatchers.not(mActivityRule.activity.window.decorView)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}