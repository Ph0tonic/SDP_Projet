package ch.epfl.sdp.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.SearchGroupData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseGroupDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
        private const val DUMMY_GROUP_NAME = "Dummy_group_name"
        private val DUMMY_LOCATION = LatLng(0.123, 23.1234)
        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @Before
    fun beforeAll() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getSearchGroupsReturnsExpectedValues() {
        val dao = FirebaseGroupDao()
        val expectedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val tested = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroups()
        data.observeForever { groups ->
            // Test once database has been populated
            if (groups != null && groups.find { it.uuid == DUMMY_GROUP_ID } != null) {
                val actualGroup = groups.find { it.uuid == DUMMY_GROUP_ID }

                MatcherAssert.assertThat(actualGroup, equalTo(expectedGroup))
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        MatcherAssert.assertThat(tested.count, equalTo(0L))
    }

    @Test
    fun getSearchGroupsByIdReturnsExpectedValues() {
        val dao = FirebaseGroupDao()
        val expectedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val tested = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroupById(DUMMY_GROUP_ID)
        data.observeForever { group ->
            // Test once database has been populated
            if (group != null) {
                MatcherAssert.assertThat(group, equalTo(expectedGroup))
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        MatcherAssert.assertThat(tested.count, equalTo(0L))
    }
}