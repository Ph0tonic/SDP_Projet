package ch.epfl.sdp

import android.os.Bundle
import android.util.Log
import android.view.View
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
            Log.w("GROUP_EDIT","${groupId}")
            Log.w("GROUP_EDIT","${groupRepo.getGroupById(groupId!!).value}")
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
        if (createGroup) {
            val searchGroupData = SearchGroupData()
            searchGroupData.name = findViewById<TextView>(R.id.group_editor_group_name).text.toString()
            groupRepo.createGroup(searchGroupData)
        } else {
            TODO("Edit existing group")
        }
        finish()
    }
}
