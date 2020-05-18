package ch.epfl.sdp.ui.search_group.edition

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.sdp.MainActivityTest
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.providers.UserRepositoryProvider
import ch.epfl.sdp.database.repository.EmptyMockSearchGroupRepo
import ch.epfl.sdp.database.repository.EmptyMockUserRepo
import ch.epfl.sdp.ui.MainActivity
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SearchGroupEditionActivityTest {

    companion object{
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"

        private val DUMMY_BASE_LOCATION = LatLng(0.0, 0.0)
        private val DUMMY_SEARCH_LOCATION = LatLng(1.0, 1.0)

        private const val DUMMY_USER_ID = "Dummy_user_id"
        private const val DUMMY_USER_EMAIL = "Dummy_user_email"
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupEditionActivity::class.java, true, false)

    @Test
    fun searchGroupEditionShowsSearchGroupNameWhenEditingAnExistingGroup(){
        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                return MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))
            }
        }
        SearchGroupRepositoryProvider.provide = {searchGroupRepo}

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_editor_group_name)).check(matches(withText(DUMMY_GROUP_NAME)))
    }

    @Test
    fun clickingOnAddOperatorButtonOpensAddUserDialog(){
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_edit_add_operator_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingOnAddRescuerButtonOpensAddUserDialog(){
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.group_edit_add_rescuer_button)).perform(click())

        onView(withId(R.id.add_user_dialog_title)).check(matches(isDisplayed()))
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfOperators(){

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                return MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))
            }
        }

        val userRepo = object : EmptyMockUserRepo(){
            override fun getOperatorsOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
                return MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.RESCUER)))
            }
        }

        SearchGroupRepositoryProvider.provide = {searchGroupRepo}
        UserRepositoryProvider.provide = {userRepo}

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)

        mActivityRule.launchActivity(intent)

        onView(withId(R.id.group_edit_operator_recyclerview)).check(matches(hasChildCount(1)))
    }

    @Test
    fun searchGroupEditShowsCorrectNumberOfRescuers(){

        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getGroupById(groupId: String): MutableLiveData<SearchGroupData> {
                return MutableLiveData(SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_BASE_LOCATION, DUMMY_SEARCH_LOCATION))
            }
        }

        val userRepo = object : EmptyMockUserRepo(){
            override fun getRescuersOfSearchGroup(searchGroupId: String): MutableLiveData<Set<UserData>> {
                return MutableLiveData(setOf(UserData(DUMMY_USER_EMAIL, DUMMY_USER_ID, Role.OPERATOR)))
            }
        }

        SearchGroupRepositoryProvider.provide = {searchGroupRepo}
        UserRepositoryProvider.provide = {userRepo}

        val intent = Intent()
        intent.putExtra(MainApplication.applicationContext().getString(R.string.intent_key_group_id), DUMMY_GROUP_ID)

        mActivityRule.launchActivity(intent)

        onView(withId(R.id.group_edit_rescuer_recyclerview)).check(matches(hasChildCount(1)))
    }
}