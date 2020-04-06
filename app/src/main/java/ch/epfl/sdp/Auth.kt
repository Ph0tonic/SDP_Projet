package ch.epfl.sdp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

object Auth : ViewModel() {

    val loggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val email: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val profileImageURL: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val name: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    init {
        GoogleSignIn.getLastSignedInAccount(MainApplication.applicationContext())
                .takeIf { account ->
                    account != null
                }
                .run {
                    login(this!!)
                }
    }

    fun logout() {
        loggedIn.postValue(false)
    }

    fun login(account: GoogleSignInAccount) {
        email.postValue(account.email)
        name.postValue(account.displayName)
        profileImageURL.postValue(account.photoUrl.toString())
        loggedIn.postValue(true)

        Log.e("Image url", account.photoUrl.toString())
        Log.e("DEBUG", "logged in")
    }
}
