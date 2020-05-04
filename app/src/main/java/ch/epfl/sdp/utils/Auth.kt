package ch.epfl.sdp.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object Auth : ViewModel() {

    private const val RC_SIGN_IN = 9001
    private var onLoginCallback: MutableList<(Boolean) -> Unit> = mutableListOf()

    val loggedIn: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val email: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val profileImageURL: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val name: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val accountId: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    init {
        GoogleSignIn.getLastSignedInAccount(MainApplication.applicationContext())
                .runCatching { updateLoginStateFromAccount(this!!) }
    }

    private fun createGoogleSignClient(): GoogleSignInClient {
        val context = MainApplication.applicationContext()
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_signin_key))
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     *  Allow to launch the connect from a Fragment or anActivity
     *  Need to override onActivityResult and call Auth.onActivityResult
     */
    fun login(fragment: Fragment, callback: ((success: Boolean) -> Unit)? = null) {
        callback?.let { onLoginCallback.add(it) }
        fragment.startActivityForResult(createGoogleSignClient().signInIntent, RC_SIGN_IN)
    }

    fun login(activity: Activity, callback: ((success: Boolean) -> Unit)? = null) {
        callback?.let { onLoginCallback.add(it) }
        activity.startActivityForResult(createGoogleSignClient().signInIntent, RC_SIGN_IN)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val context = MainApplication.applicationContext()
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener {
                        updateLoginStateFromAccount(it)
                        onLoginCallback.forEach { it(true) }
                        onLoginCallback.clear()
                    }.addOnFailureListener {
                        Toast.makeText(context, context.getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show()
                        onLoginCallback.forEach { it(false) }
                        onLoginCallback.clear()
                    }
        }
    }

    fun logout() {
        createGoogleSignClient().signOut().addOnSuccessListener {
            val context = MainApplication.applicationContext()
            loggedIn.postValue(false)
            Toast.makeText(context, context.getString(R.string.sign_out_success), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLoginStateFromAccount(account: GoogleSignInAccount) {
        email.value = account.email
        name.value = account.displayName
        profileImageURL.value = account.photoUrl.toString()
        loggedIn.value = true
        accountId.value = account.id
    }
}