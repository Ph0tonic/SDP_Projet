package ch.epfl.sdp.database.dao

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.UserData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseUserDaoTest {

    companion object {
        private const val DUMMY_GROUP_ID_1 = "Dummy_group_id_1"
        private const val DUMMY_GROUP_ID_2 = "Dummy_group_id_2"
        private const val DUMMY_GROUP_ID_3 = "Dummy_group_id_3"

        private const val DUMMY_RESCUER_ID = "Dummy_rescuer_id"
        private const val DUMMY_OPERATOR_ID = "Dummy_operator_id"
        private const val DUMMY_USER_ID = "Dummy_user_id"

        private const val DUMMY_USER_EMAIL_1 = "user1@mymail.com"
        private const val DUMMY_USER_EMAIL_2 = "user2@mymail.com"

        private const val ASYNC_CALL_TIMEOUT = 5L
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Firebase.database.goOffline()
        Firebase.database.reference.removeValue()
    }

    @Test
    fun getUsersOfGroupWithRoleGetsRescuersCorrectly() {

        val called = CountDownLatch(1)

        val expectedRescuer = UserData(DUMMY_USER_EMAIL_1, DUMMY_RESCUER_ID, Role.RESCUER)
        val controlOperator = UserData(DUMMY_USER_EMAIL_2, DUMMY_OPERATOR_ID, Role.OPERATOR)

        //Populate database
        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref.push().setValue(expectedRescuer)
        ref.push().setValue(controlOperator)

        lateinit var actualRescuer: UserData

        val dao = FirebaseUserDao()
        val actualRescuersLiveData = dao.getUsersOfGroupWithRole(DUMMY_GROUP_ID_1, Role.RESCUER)
        actualRescuersLiveData.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                actualRescuer = it.first()
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        expectedRescuer.uuid = actualRescuer.uuid

        assertThat(actualRescuer, equalTo(expectedRescuer))
    }

    @Test
    fun getUsersOfGroupWithRoleGetsOperatorsCorrectly() {

        val called = CountDownLatch(1)

        val expectedOperator = UserData(uuid = DUMMY_OPERATOR_ID, role = Role.OPERATOR)
        val controlRescuer = UserData(uuid = DUMMY_RESCUER_ID, role = Role.RESCUER)

        //Populate database
        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref.push().setValue(expectedOperator)
        ref.push().setValue(controlRescuer)

        lateinit var actualOperator: UserData

        val dao = FirebaseUserDao()
        val actualOperatorsLiveData = dao.getUsersOfGroupWithRole(DUMMY_GROUP_ID_1, Role.OPERATOR)
        actualOperatorsLiveData.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                actualOperator = it.first()
                called.countDown()
            }
        }

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        expectedOperator.uuid = actualOperator.uuid

        assertThat(actualOperator, equalTo(expectedOperator))
    }

    @Test
    fun removeUserFromSearchGroupRemovesUserFromSearchGroup() {
        val added = CountDownLatch(1)
        val called = CountDownLatch(1)

        val expectedRemovedUser = UserData(DUMMY_USER_EMAIL_1, DUMMY_USER_ID, Role.RESCUER)

        lateinit var actualRemovedUser: UserData

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                Log.w("FIREBASE", "user added")
                expectedRemovedUser.uuid = dataSnapshot.key!!
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                actualRemovedUser = dataSnapshot.getValue(UserData::class.java)!!
                actualRemovedUser.uuid = dataSnapshot.key!!
                called.countDown()
            }
        }

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedRemovedUser)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(added.count, equalTo(0L))

        val dao = FirebaseUserDao()

        dao.removeUserFromSearchGroup(DUMMY_GROUP_ID_1, expectedRemovedUser.uuid!!)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        actualRemovedUser.uuid = expectedRemovedUser.uuid
        assertThat(actualRemovedUser, equalTo(expectedRemovedUser))
        ref.removeEventListener(listener)
    }

    @Test
    fun removeAllUserOfSearchGroupRemovesAllUsersFromSearchGroup() {
        val added = CountDownLatch(2)
        val called = CountDownLatch(2)

        //They both have uuid = null to avoid complications with uuid retrieval
        val expectedRemovedUser1 = UserData(DUMMY_USER_EMAIL_1, role = Role.RESCUER)
        val expectedRemovedUser2 = UserData(DUMMY_USER_EMAIL_2, role = Role.OPERATOR)

        var actualRemovedUsers = mutableListOf<UserData>()

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedUser = dataSnapshot.getValue(UserData::class.java)!!
                actualRemovedUsers.add(removedUser)
                called.countDown()
            }
        }

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedRemovedUser1)
        ref.push().setValue(expectedRemovedUser2)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(added.count, equalTo(0L))

        val dao = FirebaseUserDao()

        dao.removeAllUserOfSearchGroup(DUMMY_GROUP_ID_1)
        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        assertThat(actualRemovedUsers, containsInAnyOrder(expectedRemovedUser1, expectedRemovedUser2))
        ref.removeEventListener(listener)
    }

    @Test
    fun addUserToSearchGroupAddsUserToSearchGroup() {
        val added = CountDownLatch(1)

        val expectedAddedUser = UserData(DUMMY_USER_EMAIL_1, role = Role.RESCUER)

        lateinit var actualAddedUser: UserData
        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                actualAddedUser = dataSnapshot.getValue(UserData::class.java)!!
                actualAddedUser.uuid = dataSnapshot.key!!
                added.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        }

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref.addChildEventListener(listener)

        val dao = FirebaseUserDao()

        dao.addUserToSearchGroup(DUMMY_GROUP_ID_1, expectedAddedUser)

        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(added.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        expectedAddedUser.uuid = actualAddedUser.uuid
        assertThat(actualAddedUser, equalTo(expectedAddedUser))
        ref.removeEventListener(listener)
    }

    @Test
    fun getGroupIdsOfUserByEmailReturnsGroupsIdsOfUser() {
        val called = CountDownLatch(4)
        val loaded = CountDownLatch(1)

        val user_1 = UserData(DUMMY_USER_EMAIL_1, role = Role.RESCUER)
        val user_2 = UserData(DUMMY_USER_EMAIL_2, role = Role.RESCUER)

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                called.countDown()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                called.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        }
        val ref = Firebase.database.getReference("users")
        ref.addChildEventListener(listener)

        val expectedIds = setOf(DUMMY_GROUP_ID_1, DUMMY_GROUP_ID_3)
        val ref1 = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")
        ref1.push().setValue(user_1)
        ref1.push().setValue(user_2)

        val ref2 = Firebase.database.getReference("users/${DUMMY_GROUP_ID_2}")
        ref2.push().setValue(user_2)

        val ref3 = Firebase.database.getReference("users/${DUMMY_GROUP_ID_3}")
        ref3.push().setValue(user_1)

        val dao = FirebaseUserDao()

        val ids = dao.getGroupIdsOfUserByEmail(user_1.email)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        ids.observeForever {
            if (it.size == 2) {
                loaded.countDown()
            }
        }

        loaded.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(loaded.count, equalTo(0L))

        // Uuid is generated automatically so we don't test
        assertThat(ids.value, equalTo(expectedIds))

        ref.removeEventListener(listener)
    }

    @Test
    fun groupsUpdateWhenUserIsAddedToExistingGroup() {
        val called = CountDownLatch(1)
        val loaded = CountDownLatch(1)

        val user1 = UserData(DUMMY_USER_EMAIL_1, role = Role.RESCUER)
        val user2 = UserData(DUMMY_USER_EMAIL_2, role = Role.RESCUER)

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                called.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        }
        val ref = Firebase.database.getReference("users")
        ref.addChildEventListener(listener)

        val expectedIds = setOf(DUMMY_GROUP_ID_1)
        val ref1 = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")

        ref1.push().setValue(user1)

        val dao = FirebaseUserDao()
        val groupIdsOfUser1 = dao.getGroupIdsOfUserByEmail(user1.email)
        val groupIdsOfUser2 = dao.getGroupIdsOfUserByEmail(user2.email)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        groupIdsOfUser1.observeForever {
            if (it.size == 1) {
                loaded.countDown()
            }
        }

        loaded.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(loaded.count, equalTo(0L))
        // Population of database finished

        ref1.push().setValue(user2)

        val loadedGroupsOfUser2 = CountDownLatch(1)

        groupIdsOfUser2.observeForever {
            if (it.size == 1) {
                loadedGroupsOfUser2.countDown()
            }
        }

        loadedGroupsOfUser2.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(loadedGroupsOfUser2.count, equalTo(0L))

        assertThat(groupIdsOfUser2.value, equalTo(expectedIds))

        ref.removeEventListener(listener)
    }

    @Test
    fun groupsUpdateWhenUserIsRemovedFromExistingGroup() {
        val called = CountDownLatch(1)
        val loaded = CountDownLatch(1)

        val user1 = UserData(DUMMY_USER_EMAIL_1, role = Role.RESCUER)
        val user2 = UserData(DUMMY_USER_EMAIL_2, role = Role.RESCUER)

        val listener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val user = dataSnapshot.children.map {
                    val user = it.getValue(UserData::class.java)!!
                    user.uuid = it.key
                    user
                }.find { user -> user.email == DUMMY_USER_EMAIL_1 }
                user1.uuid = user!!.uuid
                called.countDown()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        }
        val ref = Firebase.database.getReference("users")
        ref.addChildEventListener(listener)

        val expectedIds = setOf(DUMMY_GROUP_ID_1)
        val ref1 = Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}")

        ref1.push().setValue(user1)
        ref1.push().setValue(user2)

        val dao = FirebaseUserDao()
        val groupIdsOfUser1 = dao.getGroupIdsOfUserByEmail(user1.email)

        called.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(called.count, equalTo(0L))

        groupIdsOfUser1.observeForever {
            if (it.size == 1) {
                loaded.countDown()
            }
        }

        loaded.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(loaded.count, equalTo(0L))
        // Population of database finished

        Firebase.database.getReference("users/${DUMMY_GROUP_ID_1}/${user1.uuid}").removeValue()

        val deletedUser1FromGroup = CountDownLatch(1)

        groupIdsOfUser1.observeForever {
            if (it.isEmpty()) {
                deletedUser1FromGroup.countDown()
            }
        }

        deletedUser1FromGroup.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)
        assertThat(deletedUser1FromGroup.count, equalTo(0L))

        ref.removeEventListener(listener)
    }
}

