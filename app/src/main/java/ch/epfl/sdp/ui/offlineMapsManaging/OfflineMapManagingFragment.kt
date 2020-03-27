package ch.epfl.sdp.ui.offlineMapsManaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.R
import ch.epfl.sdp.OfflineMapManagingActivity

class OfflineMapManagingFragment : Fragment() {
    private lateinit var offlineMapManagingViewModel: OfflineMapManagingViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        offlineMapManagingViewModel = ViewModelProvider(this).get(OfflineMapManagingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_offline_map_managing, container, false)
        val textView: TextView = root.findViewById(R.id.offline_text_slideshow)
        offlineMapManagingViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = "Maps managing fragment\n(get offline maps here)"
        })

        val mapOneButton: Button = root.findViewById(R.id.stored_offline_map_1)
        mapOneButton.setOnClickListener {
            startActivity(Intent(context, OfflineMapManagingActivity::class.java).putExtra("ButtonId", 1))
        }

        val mapTwoButton: Button = root.findViewById(R.id.stored_offline_map_2)
        mapTwoButton.setOnClickListener {
            startActivity(Intent(context, OfflineMapManagingActivity::class.java).putExtra("ButtonId", 2))
        }

        val mapThreeButton: Button = root.findViewById(R.id.stored_offline_map_3)
        mapThreeButton.setOnClickListener {
            startActivity(Intent(context, OfflineMapManagingActivity::class.java).putExtra("ButtonId", 3))
        }

        val button: Button = root.findViewById(R.id.display_offline_map)
        button.setOnClickListener {
            startActivity(Intent(context, OfflineMapManagingActivity::class.java))
        }
        return root
    }


}