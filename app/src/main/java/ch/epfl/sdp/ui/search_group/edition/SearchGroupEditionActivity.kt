package ch.epfl.sdp.ui.search_group.edition

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data.UserData
import ch.epfl.sdp.database.data_manager.SearchGroupManager
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.database.repository.UserRepository
import ch.epfl.sdp.ui.search_group.OnItemClickListener

class SearchGroupEditionActivity : AppCompatActivity() {

    private var groupId: String? = null
    private var createGroup = true

    //TODO: Remove repo and user DataManager
    private val searchGroupManager = SearchGroupManager()
    private val groupRepo = SearchGroupRepository()
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_edition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))
        createGroup = groupId == null

        if (!createGroup) {
            findViewById<Button>(R.id.search_group_edition_create_or_save_button).text = getString(R.string.save_group_changes)
            groupRepo.getGroupById(groupId!!).observe(this, Observer {
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
                    userRepo.removeUserFromSearchGroup(groupId!!, user.googleId!!)
                }
            }
            userRepo.getOperatorsOfSearchGroup(groupId!!).observe(this, Observer {
                operatorsRecyclerView.adapter = UserRecyclerAdapter(it.toList(), userRemovedListener)
            })

            userRepo.getRescuersOfSearchGroup(groupId!!).observe(this, Observer {
                rescuersRecyclerView.adapter = UserRecyclerAdapter(it.toList(), userRemovedListener)
            })
        }
    }

    fun onGroupDelete(view: View) {
        //TODO: Ask for confirmation
        searchGroupManager.deleteSearchGroup(groupId!!)
        finish()
        Toast.makeText(this, "Search group deleted successfully", Toast.LENGTH_SHORT).show()
    }

    fun onGroupEditCanceled(view: View) {
        finish()
    }

    fun onGroupEditSaved(view: View) {
        val searchGroupRepo = SearchGroupRepository()
        val searchGroupData = SearchGroupData()
        //TODO also put other data
        searchGroupData.name = findViewById<TextView>(R.id.group_editor_group_name).text.toString()

        if (!validateSearchgroupData(this, searchGroupData)) {
            return
        }

        if (createGroup) {
            groupRepo.createGroup(searchGroupData)
        } else {
            searchGroupData.uuid = groupId
            groupRepo.updateGroup(searchGroupData)
        }
        finish()
    }

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
}
