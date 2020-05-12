package ch.epfl.sdp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.sdp.database.repository.SearchGroupRepository

class SearchGroupEditionActivity : AppCompatActivity() {

    private var groupId: String? = null
    private var createGroup = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_edition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getStringExtra(getString(R.string.intent_key_group_id))
        createGroup = groupId == null

        if (!createGroup) {
            TODO("Get informations of group to fill fields")
        }
    }

    fun onGroupEditCanceled(view: View) {
        finish()
    }

    fun onGroupEditSaved(view: View) {
        val searchGroupRepo = SearchGroupRepository()
        if (createGroup) {
            TODO("Create new group in repo")
        } else {
            TODO("Edit existing group")
        }
    }
}
