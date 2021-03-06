package ch.epfl.sdp.ui.search_group.edition

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.data_manager.SearchGroupDataManager
import ch.epfl.sdp.ui.dialog.DeleteConfirmDialogFragment
import ch.epfl.sdp.utils.OnItemClickListener

class SearchGroupEditionActivity : AppCompatActivity() {

    companion object {
        private fun validateSearchgroupData(searchGroupEditionActivity: SearchGroupEditionActivity, searchGroupData: SearchGroupData): Boolean {
            if (searchGroupData.name == "") {
                Toast.makeText(
                        searchGroupEditionActivity,
                        searchGroupEditionActivity.getString(R.string.search_group_edition_group_name_cannot_be_empty),
                        Toast.LENGTH_SHORT)
                        .show()
                return false
            }
            //TODO more input validation
            return true
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var groupId: String? = null
    private var createGroup = true

    private val searchGroupManager = SearchGroupDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_edition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))
        createGroup = groupId == null

        if (!createGroup) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        findViewById<Button>(R.id.search_group_edition_create_or_save_button).text = getString(R.string.save_group_changes)
        searchGroupManager.getGroupById(groupId!!).observe(this, Observer {
            if (it != null) {
                findViewById<TextView>(R.id.group_editor_group_name).text = it.name
            }
        })

        val operatorsRecyclerView = findViewById<RecyclerView>(R.id.group_edit_operator_recyclerview)
        operatorsRecyclerView.layoutManager = LinearLayoutManager(this)

        val rescuersRecyclerView = findViewById<RecyclerView>(R.id.group_edit_rescuer_recyclerview)
        rescuersRecyclerView.layoutManager = LinearLayoutManager(this)

        val userRemovedListener = object : OnItemClickListener<UserData> {
            override fun onItemClicked(user: UserData) {
                if (user.role == Role.OPERATOR && operatorsRecyclerView.adapter!!.itemCount <= 1) {
                    Toast.makeText(MainApplication.applicationContext(), getString(R.string.last_operator_cannot_be_removed), Toast.LENGTH_LONG).show()
                    return
                }
                searchGroupManager.removeUserOfSearchGroup(groupId!!, user.uuid!!)
            }
        }
        searchGroupManager.getOperatorsOfSearchGroup(groupId!!).observe(this, Observer {
            operatorsRecyclerView.adapter = UserRecyclerAdapter(it.toList(), userRemovedListener)
        })

        searchGroupManager.getRescuersOfSearchGroup(groupId!!).observe(this, Observer {
            rescuersRecyclerView.adapter = UserRecyclerAdapter(it.toList(), userRemovedListener)
        })
    }

    fun onGroupDelete(view: View) {
        val dialog = DeleteConfirmDialogFragment(getString(R.string.delete_this_search_group)) {
            searchGroupManager.deleteSearchGroup(groupId!!)
            finish()
            Toast.makeText(this, getString(R.string.search_group_deleted), Toast.LENGTH_SHORT).show()
        }
        dialog.show(supportFragmentManager, getString(R.string.delete_this_search_group))
    }

    fun onGroupEditCanceled(view: View) {
        finish()
    }

    fun onGroupEditSaved(view: View) {
        val searchGroupData = SearchGroupData()
        //TODO also put other data
        searchGroupData.name = findViewById<TextView>(R.id.group_editor_group_name).text.toString()

        if (!validateSearchgroupData(this, searchGroupData)) {
            return
        }

        if (createGroup) {
            searchGroupManager.createSearchGroup(searchGroupData.name)
        } else {
            searchGroupData.uuid = groupId
            searchGroupManager.editGroup(searchGroupData)
        }
        finish()
    }

    private fun addUser(role: Role) {
        val dialog = AddUserDialogFragment(role, object : UserAddListener {
            override fun addUser(email: String, role: Role) {
                searchGroupManager.addUserToSearchGroup(groupId!!, email, role)
            }
        })
        val tagId = if (role == Role.OPERATOR) R.string.add_an_operator else R.string.add_a_rescuer
        dialog.show(supportFragmentManager, getString(tagId))
    }

    fun addOperator(view: View) {
        addUser(Role.OPERATOR)
    }

    fun addRescuer(view: View) {
        addUser(Role.RESCUER)
    }
}
