package ch.epfl.sdp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    }
}
