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
        private const val DUMMY_GROUP_ID = "Dummy_group_id"
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

        val tested = CountDownLatch(1)

        val expectedRescuer = UserData(DUMMY_USER_EMAIL_1, DUMMY_RESCUER_ID, Role.RESCUER)
        val controlOperator = UserData(DUMMY_USER_EMAIL_2, DUMMY_OPERATOR_ID, Role.OPERATOR)

        //Populate database
        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID}")
        ref.push().setValue(expectedRescuer)
        ref.push().setValue(controlOperator)

        lateinit var actualRescuer: UserData

        val dao = FirebaseUserDao()
        val actualRescuersLiveData = dao.getUsersOfGroupWithRole(DUMMY_GROUP_ID, Role.RESCUER)
        actualRescuersLiveData.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                actualRescuer = it.first()
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        // Uuid is generated automatically so we don't test
        expectedRescuer.uuid = actualRescuer.uuid

        assertThat(actualRescuer, equalTo(expectedRescuer))
        assertThat(tested.count, equalTo(0L))
    }

    @Test
    fun getUsersOfGroupWithRoleGetsOperatorsCorrectly() {

        val tested = CountDownLatch(1)

        val expectedOperator = UserData(uuid = DUMMY_OPERATOR_ID, role = Role.OPERATOR)
        val controlRescuer = UserData(uuid = DUMMY_RESCUER_ID, role = Role.RESCUER)

        //Populate database
        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID}")
        ref.push().setValue(expectedOperator)
        ref.push().setValue(controlRescuer)

        lateinit var actualOperator: UserData

        val dao = FirebaseUserDao()
        val actualOperatorsLiveData = dao.getUsersOfGroupWithRole(DUMMY_GROUP_ID, Role.OPERATOR)
        actualOperatorsLiveData.observeForever {
            // Test once database has been populated
            if (it.isNotEmpty()) {
                actualOperator = it.first()
                tested.countDown()
            }
        }

        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        // Uuid is generated automatically so we don't test
        expectedOperator.uuid = actualOperator.uuid

        assertThat(actualOperator, equalTo(expectedOperator))
        assertThat(tested.count, equalTo(0L))
    }

    @Test
    fun removeUserFromSearchGroupRemovesUserFromSearchGroup() {
        val added = CountDownLatch(1)
        val tested = CountDownLatch(1)

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
                tested.countDown()
            }
        }

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID}")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedRemovedUser)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        val dao = FirebaseUserDao()

        dao.removeUserFromSearchGroup(DUMMY_GROUP_ID, expectedRemovedUser.uuid!!)
        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        // Uuid is generated automatically so we don't test
        actualRemovedUser.uuid = expectedRemovedUser.uuid
        assertThat(actualRemovedUser, equalTo(expectedRemovedUser))
        ref.removeEventListener(listener)
    }

    @Test
    fun removeAllUserOfSearchGroupRemovesAllUsersFromSearchGroup() {
        val added = CountDownLatch(2)
        val tested = CountDownLatch(2)

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
                tested.countDown()
            }
        }

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID}")
        ref.addChildEventListener(listener)

        //Populate database
        ref.push().setValue(expectedRemovedUser1)
        ref.push().setValue(expectedRemovedUser2)
        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        val dao = FirebaseUserDao()

        dao.removeAllUserOfSearchGroup(DUMMY_GROUP_ID)
        tested.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        assertThat(actualRemovedUsers, containsInAnyOrder(expectedRemovedUser1, expectedRemovedUser2))
        ref.removeEventListener(listener)
    }

    @Test
    fun addUserToSearchGroupAddsUserToSearchGroup(){
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

        val ref = Firebase.database.getReference("users/${DUMMY_GROUP_ID}")
        ref.addChildEventListener(listener)

        val dao = FirebaseUserDao()

        dao.addUserToSearchGroup(DUMMY_GROUP_ID, expectedAddedUser)

        added.await(ASYNC_CALL_TIMEOUT, TimeUnit.SECONDS)

        // Uuid is generated automatically so we don't test
        expectedAddedUser.uuid = actualAddedUser.uuid
        assertThat(actualAddedUser, equalTo(expectedAddedUser))
        ref.removeEventListener(listener)
    }
}