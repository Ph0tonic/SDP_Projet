package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import ch.epfl.sdp.R
import ch.epfl.sdp.database.dao.EmptyMockUserDao
import ch.epfl.sdp.database.dao.MockGroupDao
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.utils.Auth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchGroupSelectionTest {

    companion object {
        private const val DUMMY_STATION_NAME = "Dummy_station_name"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private const val DUMMY_SEARCHGROUP_ID = "Dummy_group_id"
        private const val DUMMY_USER_EMAIL = "t@gmail.com"
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupSelectionActivity::class.java, true, false)

    @Test
    fun searchGroupSelectionActivityShowsCorrectNumberOfGroups() {
        UiThreadStatement.runOnUiThread {
            Auth.loggedIn.value = true
            Auth.email.value = DUMMY_USER_EMAIL
        }

        val groupDao = MockGroupDao(
                listOf(SearchGroupData(DUMMY_SEARCHGROUP_ID, DUMMY_GROUP_NAME, null, null))
        )

        val userRepo = object : EmptyMockUserDao() {
            override fun getGroupIdsOfUserByEmail(email: String): LiveData<Set<String>> {
                return MutableLiveData(setOf(DUMMY_SEARCHGROUP_ID))
            }
        }
        SearchGroupRepository.daoProvider = { groupDao }
        UserRepository.daoProvider = { userRepo }

        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.searchGroupSelectionRecyclerview)).check(matches(hasChildCount(1)))
    }
}