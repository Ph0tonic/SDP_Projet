package ch.epfl.sdp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SearchGroupEditionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchgroup_edition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
