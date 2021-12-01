package za.co.woolworths.financial.services.android.ui.vto.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.ui.vto.ui.PermissionAction

class PermissionViewModel : ViewModel() {

    private val _actions = MutableLiveData<PermissionAction>()
    val actions: LiveData<PermissionAction> get() = _actions

    fun requestStoragePermissions() {
        _actions.postValue(PermissionAction.StoragePermissionsRequested)
    }

}