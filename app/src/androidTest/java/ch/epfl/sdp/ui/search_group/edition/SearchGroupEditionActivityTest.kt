package ch.epfl.sdp.ui.search_group.edition

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.providers.HeatmapRepositoryProvider
import ch.epfl.sdp.database.providers.MarkerRepositoryProvider
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.IHeatmapRepository
import ch.epfl.sdp.database.repository.IMarkerRepository
import ch.epfl.sdp.database.repository.ISearchGroupRepository
import ch.epfl.sdp.database.repository.IUserRepository
import ch.epfl.sdp.utils.Auth
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class SearchGroupEditionActivityTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"

        private val DUMMY_BASE_LOCATION = LatLng(0.0, 0.0)
        private val DUMMY_SEARCH_LOCATION = LatLng(1.0, 1.0)

        private const val DUMMY_USER_ID = "Dummy_user_id"
        private const val DUMMY_USER_EMAIL = "Dummy_user_email"
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupEditionActivity::class.java, true, false)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockSearchGroupRepo: ISearchGroupRepository
    private lateinit var mockHeatmapRepo: IHeatmapRepository
    private lateinit var mockMarkerRepo: IMarkerRepository
    private lateinit var mockUserRepo: IUserRepository

    @Before
    fun setup() {
        Auth.loggedIn.value = true
        Auth.email.value = DUMMY_USER_EMAIL

        mockSearchGroupRepo = Mockito.mock(ISearchGroupRepository::class.java)
        mockHeatmapRepo = Mockito.mock(IHeatmapRepository::class.java)
        mockMarkerRepo = Mockito.mock(IMarkerRepository::class.java)
        mockUserRepo = Mockito.mock(IUserRepository::class.java)

        SearchGroupRepositoryProvider.provide = { mockSearchGroupRepo }
        UserRepositoryProvider.provide = { mockUserRepo }
        MarkerRepositoryProvider.provide = { mockMarkerRepo }
        HeatmapRepositoryProvider.provide = { mockHeatmapRepo }
    }


    @Test
    fun searchGroupEditionShowsSearchGroupNameWhenEditingAnExistingGroup() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedData = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedData)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), expectedGroupId)
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_editor_group_name)).check(matches(withText(DUMMY_GROUP_NAME)))
    }

    @Test
    fun clickingOnAddOperatorButtonOpensAddUserDialog() {
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_edit_add_operator_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingOnAddRescuerButtonOpensAddUserDialog() {
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_edit_add_rescuer_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfOperators() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroups = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))
        val expectedOperators = MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.RESCUER)))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroups)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(expectedOperators)
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)

        mActivityRule.launchActivity(intent)

        onView(withId(R.id.group_edit_operator_recyclerview)).check(matches(hasChildCount(1)))
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfRescuers() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroups = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))
        val expectedRescuers = MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.RESCUER)))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroups)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(expectedRescuers)

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)

        mActivityRule.launchActivity(intent)

        onView(withId(R.id.group_edit_rescuer_recyclerview)).check(matches(hasChildCount(1)))
    }
}