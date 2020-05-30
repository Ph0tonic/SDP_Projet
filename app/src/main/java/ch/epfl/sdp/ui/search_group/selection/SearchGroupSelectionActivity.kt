package ch.epfl.sdp.ui.search_group.selection

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.data_manager.SearchGroupDataManager
import ch.epfl.sdp.ui.search_group.OnItemClickListener
import ch.epfl.sdp.ui.search_group.edition.SearchGroupEditionActivity
import ch.epfl.sdp.utils.enumStringLowerCase

class SearchGroupSelectionActivity : AppCompatActivity(), Observer<List<Pair<SearchGroupData, Role>>> {

    private lateinit var linearLayoutManager: LinearLayoutManager

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val searchGroupManager = SearchGroupDataManager()

    companion object {
        const val SEARCH_GROUP_ID_SELECTION_RESULT_TAG = "search_group"
        const val SEARCH_GROUP_ROLE_SELECTION_RESULT_TAG = "role"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_selection)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        linearLayoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.searchGroupSelectionRecyclerview)
        recyclerView.layoutManager = linearLayoutManager

        searchGroupManager.getAllGroups().observe(this, this)
    }

    override fun onChanged(searchGroups: List<Pair<SearchGroupData, Role>>) {
        val recyclerView = findViewById<RecyclerView>(R.id.searchGroupSelectionRecyclerview)
        recyclerView.adapter = SearchGroupRecyclerAdapter(searchGroups,
                object : OnItemClickListener<SearchGroupData> {
                    override fun onItemClicked(searchGroupData: SearchGroupData) {
                        joinGroup(searchGroupData, searchGroups.find { it.first == searchGroupData }!!.second)
                    }
                },
                object : OnItemClickListener<SearchGroupData> {
                    override fun onItemClicked(searchGroupData: SearchGroupData) {
                        editGroup(searchGroupData)
                    }
                })
    }

    private fun joinGroup(searchGroupData: SearchGroupData, role: Role) {
        val returnDataIntent = Intent()
        returnDataIntent.putExtra(SEARCH_GROUP_ID_SELECTION_RESULT_TAG, searchGroupData.uuid)
        returnDataIntent.putExtra(SEARCH_GROUP_ROLE_SELECTION_RESULT_TAG, role)
        setResult(RESULT_OK, returnDataIntent)
        finish()
    }

    private fun editGroup(searchGroupData: SearchGroupData) {
        val intent = Intent(MainApplication.applicationContext(), SearchGroupEditionActivity::class.java)
        intent.putExtra(getString(R.string.intent_key_group_id), searchGroupData.uuid)
        startActivity(intent)
    }

    fun addGroup(view: View) {
        val dialog = CreateSearchGroupDialogFragment(object : CreateSearchGroupListener {
            override fun createGroup(name: String) {
                val groupId = searchGroupManager.createSearchGroup(name)
                editGroup(SearchGroupData(uuid = groupId))
            }
        })
        dialog.show(supportFragmentManager, getString(R.string.create_a_search_group))
    }
}
