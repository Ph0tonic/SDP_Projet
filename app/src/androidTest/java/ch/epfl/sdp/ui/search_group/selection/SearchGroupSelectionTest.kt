package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.*
import ch.epfl.sdp.ui.search_group.edition.SearchGroupEditionActivity
import ch.epfl.sdp.utils.Auth
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SearchGroupSelectionTest {

    companion object {
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_USER_EMAIL = "t@gmail.com"
        private const val DUMMY_EMAIL = "gg@gmail.com"

        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    private lateinit var mUiDevice: UiDevice

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupSelectionActivity::class.java, true, false)

    private lateinit var mockSearchGroupRepo: ISearchGroupRepository
    private lateinit var mockHeatmapRepo: IHeatmapRepository
    private lateinit var mockMarkerRepo: IMarkerRepository
    private lateinit var mockUserRepo: IUserRepository

    @Before
    @Throws(Exception::class)
    fun before() {
        //Fake login
        runOnUiThread {
            Auth.email.value = DUMMY_EMAIL
            Auth.loggedIn.value = true
        }

        mockSearchGroupRepo = Mockito.mock(ISearchGroupRepository::class.java)
        mockHeatmapRepo = Mockito.mock(IHeatmapRepository::class.java)
        mockMarkerRepo = Mockito.mock(IMarkerRepository::class.java)
        mockUserRepo = Mockito.mock(IUserRepository::class.java)

        Mockito.`when`(mockSearchGroupRepo.getAllGroups()).thenReturn(MutableLiveData(listOf()))
        Mockito.`when`(mockUserRepo.getGroupIdsOfUserByEmail(DUMMY_EMAIL)).thenReturn(MutableLiveData(mapOf()))
        Mockito.`when`(mockSearchGroupRepo.getGroupById(DUMMY_GROUP_ID)).thenReturn(MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(DUMMY_GROUP_ID)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(DUMMY_GROUP_ID)).thenReturn(MutableLiveData(setOf()))

        SearchGroupRepositoryProvider.provide = { mockSearchGroupRepo }
        UserRepositoryProvider.provide = { mockUserRepo }
        MarkerRepositoryProvider.provide = { mockMarkerRepo }
        HeatmapRepositoryProvider.provide = { mockHeatmapRepo }

        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun cleanup() {
        SearchGroupRepositoryProvider.provide = { SearchGroupRepository() }
        UserRepositoryProvider.provide = { UserRepository() }
        MarkerRepositoryProvider.provide = { MarkerRepository() }
        HeatmapRepositoryProvider.provide = { HeatmapRepository() }
    }

    @Test
    fun searchGroupSelectionActivityShowsCorrectNumberOfGroups() {
        runOnUiThread {
            Auth.loggedIn.value = true
            Auth.email.value = DUMMY_USER_EMAIL
        }

        val isManagerReady = CountDownLatch(1)

        val userRepoMock = Mockito.mock(IUserRepository::class.java)
        Mockito.`when`(userRepoMock.getGroupIdsOfUserByEmail(DUMMY_USER_EMAIL)).thenReturn(MutableLiveData(mapOf(Pair(DUMMY_GROUP_ID, Role.OPERATOR))))
        UserRepositoryProvider.provide = { userRepoMock }

        val searchGroupRepoMock = Mockito.mock(ISearchGroupRepository::class.java)
        Mockito.`when`(searchGroupRepoMock.getAllGroups()).thenReturn(
                MutableLiveData(listOf(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME)))
        )
        SearchGroupRepositoryProvider.provide = { searchGroupRepoMock }

        mActivityRule.launchActivity(Intent())

        runOnUiThread {
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

    @Test
    fun addSearchGroupButtonOpensDialog() {
        mActivityRule.launchActivity(Intent())
        onView(withId(R.id.create_new_search_group)).perform(click())

        onView(withId(R.id.dialog_create_searchgroup)).check(matches(isDisplayed()))
    }

    @Test
    fun addSearchGroupButtonOpensDialogAndCallbackOpenEditSearchGroupActivity() {
        mActivityRule.launchActivity(Intent())
        Mockito.`when`(mockSearchGroupRepo.createGroup(SearchGroupData(name = DUMMY_GROUP_NAME))).thenReturn(DUMMY_GROUP_ID)

        onView(withId(R.id.create_new_search_group)).perform(click())

        onView(withId(R.id.dialog_create_searchgroup)).check(matches(isDisplayed()))

        onView(withId(R.id.create_search_group_name)).perform(typeText(DUMMY_GROUP_NAME))
        mUiDevice.pressBack() //Required in case of landscape orientation
        onView(withId(R.id.dialog_create_searchgroup)).perform(click())

        intended(allOf(hasComponent(SearchGroupEditionActivity::class.java.name), hasExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)))
    }
}