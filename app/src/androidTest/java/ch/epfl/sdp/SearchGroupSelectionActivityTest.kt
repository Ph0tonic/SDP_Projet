package ch.epfl.sdp

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.database.dao.MockGroupDao
import ch.epfl.sdp.database.dao.UserDao
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.ui.MainActivity
import ch.epfl.sdp.utils.Auth
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class SearchGroupEditionActivityTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(MainActivity::class.java, true, false)

    private lateinit var mUiDevice: UiDevice

    @Before
    @Throws(Exception::class)
    fun before() {
        runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
            Auth.loggedIn.value = true
        }
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    companion object {
        private const val DUMMY_SEARCHGROUP_ID = "DummySearchGroupId"
        private const val DUMMY_SEARCHGROUP_NAME = "DummySearchGroupName"
        private const val DUMMY_USER_EMAIL = "dummy@gmail.com"
        private val DUMMY_LOCATION = LatLng(0.0, 0.0)
        private const val FAKE_ACCOUNT_ID = "FakeAccountId"
    }

    @Test
    fun groupNamesAreDisplayed() {
        runOnUiThread {
            Auth.loggedIn.value = true
            Auth.email.value = DUMMY_USER_EMAIL
        }

        val groupDao = MockGroupDao(
                listOf(SearchGroupData(DUMMY_SEARCHGROUP_ID, DUMMY_SEARCHGROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION))
        )

        val userDao = Mockito.mock(UserDao::class.java)
        Mockito.`when`(userDao.getGroupIdsOfUserByEmail(DUMMY_USER_EMAIL)).thenReturn(MutableLiveData(mapOf(Pair(DUMMY_SEARCHGROUP_ID, Role.OPERATOR))))

        SearchGroupRepository.daoProvider = { groupDao }
        UserRepository.daoProvider = { userDao }

        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.search_group_selection_button)).perform(click())
        onView(withText(DUMMY_SEARCHGROUP_NAME)).check(matches(isDisplayed()))
    }

    @Test
    fun changingGroupChangesGroupInPreferences() {
        val groupDao = MockGroupDao(
                listOf(SearchGroupData(DUMMY_SEARCHGROUP_ID, DUMMY_SEARCHGROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION))
        )
        SearchGroupRepository.daoProvider = { groupDao }
        PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .edit().putString(
                        MainApplication.applicationContext().getString(R.string.pref_key_current_group_id), "")
                .apply()

        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.search_group_selection_button)).perform(click())
        onView(withText(DUMMY_SEARCHGROUP_NAME)).perform(click())
        val actualGroupId = PreferenceManager.getDefaultSharedPreferences(MainApplication.applicationContext())
                .getString(mActivityRule.activity.getString(R.string.pref_key_current_group_id), null)
        assertThat(actualGroupId, equalTo(DUMMY_SEARCHGROUP_ID))
    }
}