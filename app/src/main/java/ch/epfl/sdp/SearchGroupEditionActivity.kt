package ch.epfl.sdp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.repository.SearchGroupRepository

class SearchGroupEditionActivity : AppCompatActivity() {

    private var groupId: String? = null
    private var createGroup = true

    private val groupRepo = SearchGroupRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_edition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))
        createGroup = groupId == null

        if (!createGroup) {
            findViewById<Button>(R.id.search_group_edition_create_or_save_button).text = getString(R.string.save_group_changes)
            val groupData = groupRepo.getGroupById(groupId!!).observe(this, Observer {
                if (it != null){
                    findViewById<TextView>(R.id.group_editor_group_name).text = it.name
                }
            })
        }
    }

    fun onGroupEditCanceled(view: View) {
        finish()
    }

    fun onGroupEditSaved(view: View) {
        val searchGroupRepo = SearchGroupRepository()
        val searchGroupData = SearchGroupData()
        //TODO also put other data
        //TODO input validation
        searchGroupData.name = findViewById<TextView>(R.id.group_editor_group_name).text.toString()
        if (createGroup) {
            groupRepo.createGroup(searchGroupData)
        } else {
            searchGroupData.uuid = groupId
            groupRepo.updateGroup(searchGroupData)
        }
        finish()
    }
}
