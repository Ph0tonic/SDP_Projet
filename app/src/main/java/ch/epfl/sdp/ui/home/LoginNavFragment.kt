package ch.epfl.sdp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ch.epfl.sdp.Auth
import ch.epfl.sdp.R
import com.bumptech.glide.Glide

class LoginNavFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_nav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.nav_login_button).setOnClickListener {
            Auth.login(this)
        }

        Auth.email.observe(viewLifecycleOwner, Observer { email ->
            view.findViewById<TextView>(R.id.nav_user_email).text = email
        })

        Auth.name.observe(viewLifecycleOwner, Observer { name ->
            view.findViewById<TextView>(R.id.nav_username).text = name
        })

        Auth.profileImageURL.observe(viewLifecycleOwner, Observer { imageURL ->
            Glide
                    .with(context)
                    .load(imageURL)
                    .error(R.mipmap.ic_launcher_round)
                    .into(view.findViewById(R.id.nav_user_image))
        })

        val visibility: (Boolean) -> Int = { visible -> if (visible) View.VISIBLE else View.GONE }
        Auth.loggedIn.observe(viewLifecycleOwner, Observer { loggedIn ->
            view.findViewById<TextView>(R.id.nav_username).visibility = visibility(loggedIn)
            view.findViewById<TextView>(R.id.nav_user_email).visibility = visibility(loggedIn)
            view.findViewById<ImageView>(R.id.nav_user_image).visibility = visibility(loggedIn)
            view.findViewById<ImageView>(R.id.nav_user_image_default).visibility = visibility(!loggedIn)
            view.findViewById<Button>(R.id.nav_login_button).visibility = visibility(!loggedIn)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth.onActivityResult(requestCode, resultCode, data)
    }
}
