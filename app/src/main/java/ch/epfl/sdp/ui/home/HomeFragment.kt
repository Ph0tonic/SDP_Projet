package ch.epfl.sdp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.epfl.sdp.R


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //TODO Clean of code
//        root.findViewById<Button>(R.id.display_map).setOnClickListener {
//            val intent = Intent(context, MapActivity::class.java)
//                    .putExtra("groupId","g2") //TODO adapt group ID via group choosing activity or something
//            startActivity(intent)
//        }
        return root
    }
}
