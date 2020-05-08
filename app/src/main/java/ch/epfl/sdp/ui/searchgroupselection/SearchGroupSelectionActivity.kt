package ch.epfl.sdp.ui.searchgroupselection

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData
import ch.epfl.sdp.database.repository.SearchGroupRepository

class SearchGroupSelectionActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        const val SEARH_GROUP_ID_SELECTION_RESULT_TAG = "search_group"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_group_selection)

        linearLayoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.searchGroupSelectionRecyclerview)
        recyclerView.layoutManager = linearLayoutManager

        SearchGroupRepository().getGroups().observe(this, Observer {
            recyclerView.adapter = SearchGroupRecyclerAdapter(it, this)
        })
    }

    override fun onItemClicked(searchGroupData: SearchGroupData) {
        val data = Intent();
        data.putExtra(SEARH_GROUP_ID_SELECTION_RESULT_TAG,searchGroupData.uuid)
        setResult(RESULT_OK, data);
        finish();
    }
}
