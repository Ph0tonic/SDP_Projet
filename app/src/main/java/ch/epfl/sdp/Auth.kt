package ch.epfl.sdp

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.sdp.MainApplication.Companion.applicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object Auth : ViewModel() {

    private const val RC_SIGN_IN = 9001
    private var googleSignInClient: GoogleSignInClient

    val loggedIn: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val email: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val profileImageURL: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val name: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    init {
        val context = applicationContext()
        GoogleSignIn.getLastSignedInAccount(context)
                .runCatching {
                    updateLoginStateFromAccount(this!!)
                }
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_signin_key))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     *  Allow to launch the connect from a Fragment or anActivity
     *  Need to override onActivityResult and call Auth.onActivityResult
     */
    fun login(fragment: Fragment) {
        fragment.startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    fun login(activity: Activity) {
        activity.startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val context = applicationContext()
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener {
                        updateLoginStateFromAccount(it)
                    }.addOnFailureListener {
                        Toast.makeText(context, context.getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show()
                    }
        }
    }

    fun logout() {
        googleSignInClient.signOut().addOnSuccessListener { loggedIn.postValue(false) }
    }

    private fun updateLoginStateFromAccount(account: GoogleSignInAccount) {
        email.postValue(account.email)
        name.postValue(account.displayName)
        profileImageURL.postValue(account.photoUrl.toString())
        loggedIn.postValue(true)
    }
}
