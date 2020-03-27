package ch.epfl.sdp.ui.offlineMapsManaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OfflineMapManagingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value  = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text
}