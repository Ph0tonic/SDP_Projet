package ch.epfl.sdp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

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
                    email.postValue(this?.email)
                    name.postValue(this?.displayName)
                    profileImageURL.postValue(this?.photoUrl.toString())
                    loggedIn.postValue(true)
                    Log.e("DEBUG", "logged in")
                }
    }
}
