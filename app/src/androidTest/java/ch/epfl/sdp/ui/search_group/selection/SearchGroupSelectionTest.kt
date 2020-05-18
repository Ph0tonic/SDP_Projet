package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.providers.SearchGroupRepositoryProvider
import ch.epfl.sdp.database.repository.EmptyMockSearchGroupRepo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchGroupSelectionTest {

    companion object{
        private const val DUMMY_STATION_NAME = "Dummy_station_name"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
    }

    @get:Rule
    var mActivityRule = IntentsTestRule(SearchGroupSelectionActivity::class.java, true, false)

    @Test
    fun searchGroupSelectionActivityShowsCorrectNumberOfGroups(){
        val searchGroupRepo = object : EmptyMockSearchGroupRepo() {
            override fun getAllGroups(): MutableLiveData<List<SearchGroupData>> {
                return MutableLiveData(listOf(SearchGroupData(DUMMY_GROUP_NAME)))
            }
        }
        SearchGroupRepositoryProvider.provide = {searchGroupRepo}

        mActivityRule.launchActivity(Intent())

        onView(withId(R.id.searchGroupSelectionRecyclerview)).check(matches(hasChildCount(1)))
    }
}