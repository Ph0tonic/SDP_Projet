package ch.epfl.sdp.ui.search_group.edition

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
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
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class SearchGroupEditionActivityTest {

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"

        private const val DUMMY_USER_ID = "Dummy_user_id"
        private const val DUMMY_USER_EMAIL = "dummyuseremail@gmail.com"
    }

    private lateinit var mUiDevice: UiDevice

    private val intentAddition = Intent()
    private val intentEdition = Intent()
            .putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)

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

        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }


    @Test
    fun searchGroupEditionShowsSearchGroupNameWhenEditingAnExistingGroup() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedData = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedData)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        mActivityRule.launchActivity(intentEdition)
        onView(withId(R.id.group_editor_group_name)).check(matches(withText(DUMMY_GROUP_NAME)))
    }

    @Test
    fun clickingOnAddOperatorButtonOpensAddUserDialog() {
        mActivityRule.launchActivity(intentAddition)
        onView(withId(R.id.group_edit_add_operator_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingOnAddRescuerButtonOpensAddUserDialog() {
        mActivityRule.launchActivity(intentAddition)
        onView(withId(R.id.group_edit_add_rescuer_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun addingAnOperatorAddsAnOperator() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroup = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroup)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        mActivityRule.launchActivity(intentEdition)

        val expectedOperator = UserData(DUMMY_USER_EMAIL, role = Role.OPERATOR)

        onView(withId(R.id.group_edit_add_operator_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
        onView(withId(R.id.add_user_email_address)).perform(ViewActions.typeText(DUMMY_USER_EMAIL))
        mUiDevice.pressBack()
        onView(withId(R.id.dialog_add_user)).perform(click())

        Mockito.verify(mockUserRepo, Mockito.times(1)).addUserToSearchGroup(DUMMY_GROUP_ID, expectedOperator)
    }

    @Test
    fun addingARescuerAddsARescuer() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroup = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroup)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        mActivityRule.launchActivity(intentEdition)

        val expectedRescuer = UserData(DUMMY_USER_EMAIL, role = Role.RESCUER)

        onView(withId(R.id.group_edit_add_rescuer_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
        onView(withId(R.id.add_user_email_address)).perform(ViewActions.typeText(DUMMY_USER_EMAIL))
        mUiDevice.pressBack()
        onView(withId(R.id.dialog_add_user)).perform(click())

        Mockito.verify(mockUserRepo, Mockito.times(1)).addUserToSearchGroup(DUMMY_GROUP_ID, expectedRescuer)
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfOperators() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroup = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))
        val expectedOperators = MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.RESCUER)))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroup)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(expectedOperators)
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))

        mActivityRule.launchActivity(intentEdition)

        onView(withId(R.id.group_edit_operator_recyclerview)).check(matches(hasChildCount(1)))
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfRescuers() {
        val expectedGroupId = DUMMY_GROUP_ID
        val expectedGroup = MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME))
        val expectedRescuers = MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.RESCUER)))

        Mockito.`when`(mockSearchGroupRepo.getGroupById(expectedGroupId)).thenReturn(expectedGroup)
        Mockito.`when`(mockUserRepo.getOperatorsOfSearchGroup(expectedGroupId)).thenReturn(MutableLiveData(setOf()))
        Mockito.`when`(mockUserRepo.getRescuersOfSearchGroup(expectedGroupId)).thenReturn(expectedRescuers)

        mActivityRule.launchActivity(intentEdition)

        onView(withId(R.id.group_edit_rescuer_recyclerview)).check(matches(hasChildCount(1)))
    }
}