package za.co.woolworths.financial.services.android.ui.vto.presentation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SetImageViewModel : ViewModel() {

    private val _userImage = MutableLiveData<Uri>()
    val userImage: LiveData<Uri> get() = _userImage

    fun setUserImage(uri: Uri?) {
        _userImage.postValue(uri!!)
    }


}