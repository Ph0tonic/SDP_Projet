package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.SearchGroupData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
        private const val DUMMY_GROUP_NAME_2 = "Dummy_group_name2"
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
        val called = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroups()
        data.observeForever { groups ->
            // Test once database has been populated
            if (groups != null && groups.find { it.uuid == DUMMY_GROUP_ID } != null) {
                val actualGroup = groups.find { it.uuid == DUMMY_GROUP_ID }

                assertThat(actualGroup, equalTo(expectedGroup))
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun getSearchGroupsByIdReturnsExpectedValues() {
        val dao = FirebaseGroupDao()
        val expectedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val called = CountDownLatch(1)

        //Populate database
        Firebase.database.getReference("search_groups/$DUMMY_GROUP_ID")
                .setValue(expectedGroup)

        //Validate g1 data
        val data = dao.getGroupById(DUMMY_GROUP_ID)
        data.observeForever { group ->
            // Test once database has been populated
            if (group != null) {
                assertThat(group, equalTo(expectedGroup))
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))
    }

    @Test
    fun createGroupCreatesGroup() {
        val called = CountDownLatch(1)

        val expectedAddedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        lateinit var actualAddedGroup: SearchGroupData

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                actualAddedGroup = dataSnapshot.getValue(SearchGroupData::class.java)!!
                actualAddedGroup.uuid = dataSnapshot.key!!
                called.countDown()
            }
        }

        val ref = Firebase.database.getReference("search_groups")
        ref.addChildEventListener(listener)

        val dao = FirebaseGroupDao()

        val groupId = dao.createGroup(expectedAddedGroup)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        expectedAddedGroup.uuid = actualAddedGroup.uuid
        assertThat(groupId, equalTo(expectedAddedGroup.uuid))
        assertThat(actualAddedGroup, equalTo(expectedAddedGroup))
        ref.removeEventListener(listener)
    }

    @Test
    fun removeSearchGroupRemovesSearchGroup() {
        val added = CountDownLatch(1)
        val called = CountDownLatch(1)

        val expectedRemovedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        lateinit var actualRemovedGroup: SearchGroupData

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                expectedRemovedGroup.uuid = dataSnapshot.key!!
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                actualRemovedGroup = dataSnapshot.getValue(SearchGroupData::class.java)!!
                actualRemovedGroup.uuid = dataSnapshot.key!!
                called.countDown()
            }
        }

        val ref = Firebase.database.getReference("search_groups")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedRemovedGroup)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(added.count, equalTo(0L))

        val dao = FirebaseGroupDao()

        dao.removeSearchGroup(expectedRemovedGroup.uuid!!)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        actualRemovedGroup.uuid = expectedRemovedGroup.uuid
        assertThat(actualRemovedGroup, equalTo(expectedRemovedGroup))
        ref.removeEventListener(listener)
    }

    @Test
    fun updateSearchGroupUpdatesSearchGroup() {
        val added = CountDownLatch(1)
        val called = CountDownLatch(1)

        val expectedAddedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME, DUMMY_LOCATION, DUMMY_LOCATION)
        val expectedUpdatedGroup = SearchGroupData(DUMMY_GROUP_ID, DUMMY_GROUP_NAME_2, DUMMY_LOCATION, DUMMY_LOCATION)
        lateinit var actualUpdatedGroup: SearchGroupData

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                actualUpdatedGroup = dataSnapshot.getValue(SearchGroupData::class.java)!!
                actualUpdatedGroup.uuid = dataSnapshot.key!!
                called.countDown()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                expectedUpdatedGroup.uuid = dataSnapshot.key!!
                added.countDown()
            }
        }

        val ref = Firebase.database.getReference("search_groups")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedAddedGroup)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(added.count, equalTo(0L))

        val dao = FirebaseGroupDao()

        dao.updateGroup(expectedUpdatedGroup)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        actualUpdatedGroup.uuid = expectedUpdatedGroup.uuid
        assertThat(actualUpdatedGroup, equalTo(expectedUpdatedGroup))
        ref.removeEventListener(listener)
    }
}