package ch.epfl.sdp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.database.repository.SearchGroupRepository
import ch.epfl.sdp.ui.searchgroupselection.SearchGroupRecyclerAdapter

class SearchGroupSelectionActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        const val CANCELLED_RESULT_TAG = "cancelled"
        const val SEARH_GROUP_ID_SELECTION_RESULT_TAG = "search_group"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_group_selection)

        linearLayoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.searchGroupSelectionRecyclerview)
        recyclerView.layoutManager = linearLayoutManager

        SearchGroupRepository().getGroups().observe(this, Observer {
            recyclerView.adapter = SearchGroupRecyclerAdapter(it)
        })
    }
}
