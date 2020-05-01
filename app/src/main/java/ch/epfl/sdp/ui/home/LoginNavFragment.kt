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
import ch.epfl.sdp.utils.Auth
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

        view.findViewById<TextView>(R.id.nav_signin_button).setOnClickListener {
            Auth.login(this)
        }
        setOf(Auth.email, Auth.name).zip(setOf(R.id.nav_user_email, R.id.nav_username))
                .forEach {
                    it.first.observe(viewLifecycleOwner, Observer { value ->
                        view.findViewById<TextView>(it.second).text = value
                    })
                }

        Auth.profileImageURL.observe(viewLifecycleOwner, Observer { imageURL ->
            Glide.with(context).load(imageURL).error(R.mipmap.ic_launcher_round)
                    .into(view.findViewById(R.id.nav_user_image))
        })

        val visibility: (Boolean) -> Int = { visible -> if (visible) View.VISIBLE else View.GONE }

        Auth.loggedIn.observe(viewLifecycleOwner, Observer { loggedIn ->
            setOf(R.id.nav_username, R.id.nav_user_email)
                    .forEach { view.findViewById<TextView>(it).visibility = visibility(loggedIn) }

            setOf(R.id.nav_user_image, R.id.nav_user_image_default)
                    .zip(setOf(loggedIn, !loggedIn).map { visibility(it) })
                    .forEach { view.findViewById<ImageView>(it.first).visibility = it.second }

            view.findViewById<Button>(R.id.nav_signin_button).visibility = visibility(!loggedIn)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth.onActivityResult(requestCode, resultCode, data)
    }
}
