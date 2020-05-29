package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.IHeatmapRepository
import ch.epfl.sdp.database.repository.IMarkerRepository
import ch.epfl.sdp.database.repository.ISearchGroupRepository
import ch.epfl.sdp.database.repository.IUserRepository
import ch.epfl.sdp.utils.Auth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class CreateSearchGroupDialogFragmentTest {

    companion object {
        private const val FAKE_ACCOUNT_ID = "fake_account_id"
        private const val DUMMY_GROUP_ID = "DummyGroupId"
        private const val DUMMY_GROUP_NAME = "DummyGroupName"
        private const val VALID_DUMMY_NAME = "DummyGroupId@gmail.com"
        private const val EMPTY_DUMMY_NAME = ""
        private const val DUMMY_EMAIL = "gg@gmail.com"
    }

    private lateinit var mUiDevice: UiDevice

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mActivityRule = IntentsTestRule(
            SearchGroupSelectionActivity::class.java,
            true,
            false) // Activity is not launched immediately

    private lateinit var mockSearchGroupRepo: ISearchGroupRepository
    private lateinit var mockHeatmapRepo: IHeatmapRepository
    private lateinit var mockMarkerRepo: IMarkerRepository
    private lateinit var mockUserRepo: IUserRepository

    @Before
    @Throws(Exception::class)
    fun before() {
        //Fake login
        UiThreadStatement.runOnUiThread {
            Auth.accountId.value = FAKE_ACCOUNT_ID
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

    @Test
    fun whenValidateDialogWitEmptyNameDoNotAccept() {
        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.create_new_search_group)).perform(click())

        onView(withId(R.id.create_search_group_name)).perform(typeText(EMPTY_DUMMY_NAME))
        onView(withId(R.id.dialog_create_searchgroup)).perform(click())

        onView(withId(R.id.create_search_group_name)).check(matches(hasErrorText(mActivityRule.activity.getString(R.string.search_group_name_cannot_be_empty))))
    }

    @Test
    fun whenValidateDialogWitValidNameAccept() {
        mActivityRule.launchActivity(Intent())

        Mockito.`when`(mockSearchGroupRepo.createGroup(SearchGroupData(name = DUMMY_GROUP_NAME))).thenReturn(DUMMY_GROUP_ID)
        onView(withId(R.id.create_new_search_group)).perform(click())

        onView(withId(R.id.create_search_group_name)).perform(typeText(VALID_DUMMY_NAME))
        mUiDevice.pressBack() //Required in case of landscape orientation
        onView(withId(R.id.dialog_create_searchgroup)).perform(click())

        onView(withId(R.id.dialog_create_searchgroup)).check(doesNotExist())
    }

    @Test
    fun whenCancelDialogThenDismissDialog() {
        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.create_new_search_group)).perform(click())
        onView(withId(R.id.dialog_cancel_create_searchgroup)).perform(click())

        onView(withId(R.id.dialog_cancel_create_searchgroup)).check(doesNotExist())
    }
}