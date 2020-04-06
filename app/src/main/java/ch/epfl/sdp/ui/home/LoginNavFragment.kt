package ch.epfl.sdp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginNavFragment : Fragment() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_nav, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("DEBUG", "Set listener")

        view.findViewById<TextView>(R.id.nav_login_button).setOnClickListener {
            startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
        }

        Auth.email.observe(viewLifecycleOwner, Observer { email ->
            view.findViewById<TextView>(R.id.nav_username).text = email
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
        Auth.loggedIn.observe(viewLifecycleOwner, Observer { loggedIn ->
            val visibility = if (loggedIn) View.VISIBLE else View.GONE
            view.findViewById<TextView>(R.id.nav_username).visibility = visibility
            view.findViewById<TextView>(R.id.nav_user_email).visibility = visibility
            view.findViewById<ImageView>(R.id.nav_user_image).visibility = visibility
            view.findViewById<Button>(R.id.nav_login_button).visibility = if (loggedIn) View.GONE else View.VISIBLE
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_signin_key))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account: GoogleSignInAccount? = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                //updateUserView(account?.displayName, account?.email, account?.photoUrl.toString())
            } catch (e: ApiException) {
//                Snackbar.make(findViewById(R.id.main_nav_header), "Could not sign in :(", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show()
            }
        }
    }
}
