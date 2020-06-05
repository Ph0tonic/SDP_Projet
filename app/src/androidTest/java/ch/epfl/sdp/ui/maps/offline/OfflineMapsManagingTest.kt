package ch.epfl.sdp.ui.maps.offline

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.map.MapUtils
import ch.epfl.sdp.ui.MainActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineMapsManagingTest {

    private lateinit var mUiDevice: UiDevice
    private lateinit var offlineManager: OfflineManager

    companion object {
        private const val FAKE_MAP_NAME_1 = "RandomName_1"
        private const val FAKE_MAP_NAME_2 = "RandomName_2"
        private const val POSITIVE_BUTTON_ID = android.R.id.button1
        private const val MAP_DOWNLOADING_TIMEOUT = 4000L
        private const val MAP_LOADING_TIMEOUT = 1000L
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
        offlineManager = OfflineManager.getInstance(MainApplication.applicationContext())
    }

    private fun openDrawer() {
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())
    }

    @Test
    fun canNavigateToMapsManaging() {
        openDrawer()
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing))
    }

    @Test
    fun canLaunchOfflineManagerActivity() {
        openDrawer()
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing))
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.no_offline_map_downloaded_yet))), MAP_LOADING_TIMEOUT)

        onView(withId(R.id.store_button)).perform(click())

        Intents.intended(IntentMatchers.hasComponent(OfflineManagerActivity::class.java.name))
    }

    @Test
    fun canNavigateToDownloadedMap() {
        MapUtils.saveCameraPositionAndZoomToPrefs(
                MapUtils.getCameraWithParameters(
                        LatLng(0.0, 0.0),
                        20.0
                )
        )

        openDrawer()
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing))
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.no_offline_map_downloaded_yet))), MAP_LOADING_TIMEOUT)

        onView(withId(R.id.store_button)).perform(click())
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.dialog_positive_button))), MAP_LOADING_TIMEOUT)

        //DOWNLOAD Part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(FAKE_MAP_NAME_1))
        mUiDevice.pressBack()

        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        mUiDevice.wait(Until.hasObject(By.desc(FAKE_MAP_NAME_1)), MAP_DOWNLOADING_TIMEOUT)

        onView((withText(FAKE_MAP_NAME_1))).perform(click())
        onView(withText(R.string.delete)).check(matches(isDisplayed()))

        //DELETE PART
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
                offlineRegions.forEach {
                    it.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                        override fun onDelete() {}
                        override fun onError(error: String?) {}
                    })
                }
            }

            override fun onError(error: String?) {}
        })
    }

    @Test
    fun canDeleteDownloadedMap() {
        MapUtils.saveCameraPositionAndZoomToPrefs(
                MapUtils.getCameraWithParameters(
                        LatLng(0.0, 0.0),
                        20.0
                )
        )

        openDrawer()
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_maps_managing))
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.no_offline_map_downloaded_yet))), MAP_LOADING_TIMEOUT)

        onView(withId(R.id.store_button))
                .perform(click())
        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.dialog_positive_button))), MAP_LOADING_TIMEOUT)

        //DOWNLOAD Part
        onView(withId(R.id.download_button)).perform(click())
        onView(withId(R.id.dialog_textfield_id)).perform(typeText(FAKE_MAP_NAME_2))
        mUiDevice.pressBack()

        onView(withId(POSITIVE_BUTTON_ID)).perform(click())
        mUiDevice.wait(Until.hasObject(By.desc(FAKE_MAP_NAME_2)), MAP_DOWNLOADING_TIMEOUT)

        onView((withText(FAKE_MAP_NAME_2))).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.delete))), MAP_DOWNLOADING_TIMEOUT)
        onView(withId(R.id.delete_offline_map_button)).perform(click())
        onView(withText(R.string.delete)).perform(click())

        mUiDevice.wait(Until.hasObject(By.desc(MainApplication.applicationContext().getString(R.string.no_offline_map_downloaded_yet))), MAP_DOWNLOADING_TIMEOUT)
        onView(withText(R.string.no_offline_map_downloaded_yet)).check(matches(isDisplayed()))
    }
}