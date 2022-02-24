package za.co.woolworths.financial.services.android.ui.fragments.account.main.fragment.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.extension.fromJson
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.IBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.INavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    bottomSheet: WBottomSheetBehaviour,
    graph: NavigationGraph
) : ViewModel(),
    IBottomSheetBehaviour by bottomSheet,
    INavigationGraph by graph {

    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account>
        get() = _account

    /***
     * TODO:: Convert AccountResponse to Parcelable to remove gson Convertor
     * will be achievable after my account landing refactoring
     */

    fun convertStringToAccountObject(value: String){
        val gSon = Gson().fromJson<Account>(value)
        _account.postValue(gSon)
    }
}



