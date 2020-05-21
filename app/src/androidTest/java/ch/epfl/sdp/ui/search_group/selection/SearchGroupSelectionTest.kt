package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.ISearchGroupRepository
import ch.epfl.sdp.database.repository.IUserRepository
import ch.epfl.sdp.utils.Auth
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SearchGroupSelectionTest {

    companion object {
        private const val DUMMY_STATION_NAME = "Dummy_station_name"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_USER_EMAIL = "t@gmail.com"

        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupSelectionActivity::class.java, true, false)

    @Test
    fun searchGroupSelectionActivityShowsCorrectNumberOfGroups() {
        UiThreadStatement.runOnUiThread {
            Auth.loggedIn.value = true
            Auth.email.value = DUMMY_USER_EMAIL
        }

        val isManagerReady = CountDownLatch(1)

        val userRepoMock = Mockito.mock(IUserRepository::class.java)
        Mockito.`when`(userRepoMock.getGroupIdsOfUserByEmail(DUMMY_USER_EMAIL)).thenReturn(MutableLiveData(setOf(DUMMY_GROUP_ID)))
        UserRepositoryProvider.provide = { userRepoMock }

        val searchGroupRepoMock = Mockito.mock(ISearchGroupRepository::class.java)
        Mockito.`when`(searchGroupRepoMock.getAllGroups()).thenReturn(
                MutableLiveData(listOf(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)))
        )
        SearchGroupRepositoryProvider.provide = { searchGroupRepoMock }

        mActivityRule.launchActivity(Intent())

        runOnUiThread{
            mActivityRule.activity.searchGroupManager.getAllGroups().observe(mActivityRule.activity, Observer {
                if (it != null) {
                    isManagerReady.countDown()
                }
            })
        }

        isManagerReady.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(isManagerReady.count, equalTo(0L))

        onView(withId(R.id.searchGroupSelectionRecyclerview)).check(matches(hasChildCount(1)))
    }
}