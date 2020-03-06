package ch.epfl.sdp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private var loggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("100617880241-gngllqsmp0tnbor8r2sr4r396t2hfj42.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        updateUI()

        google_login_btn.setOnClickListener { signIn() }
        google_logout_btn.setOnClickListener { signOut() }
    }

    private fun updateUI() {
        google_login_btn.visibility = computeVisibility(loggedIn)
        google_logout_btn.visibility = computeVisibility(!loggedIn)
    }

    private fun computeVisibility(status: Boolean): Int {
        return if (status) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        loggedIn = GoogleSignIn.getLastSignedInAccount(this) == null
        Log.i("SignIn interface", "Opened connecting status : $loggedIn")
    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            loggedIn = false
            updateUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                loggedIn = true
            } catch (e: ApiException) {
                // Sign in was unsuccessful
            }
            updateUI()
        }
    }

//    companion object {
//        fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) : Boolean {
//            return try {
//                val account = completedTask
//                // Signed in successfully
//                Log.i("Google ID", account?.id ?: "")
//                Log.i("Google First/Last Name", account?.givenName ?: "" + account?.familyName ?: "")
//                Log.i("Google ID Token", account?.idToken ?: "")
//                true
//            } catch (e: ApiException) {
//                // Sign in was unsuccessful
//                Log.e("failed code=", e.statusCode.toString())
//                false
//            }
//        }
//    }

}
