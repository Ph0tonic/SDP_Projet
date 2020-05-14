package ch.epfl.sdp.ui.vlc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VlcViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is VLC Fragment"
    }
    val text: LiveData<String> = _text
}