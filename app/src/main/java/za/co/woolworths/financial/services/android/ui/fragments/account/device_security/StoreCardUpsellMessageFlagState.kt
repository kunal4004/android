package za.co.woolworths.financial.services.android.ui.fragments.account.device_security

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

interface StoreCardUpsellMessageFlag{
    fun activateFreezeStoreCardFlag()
    fun disableFreezeStoreCardFlag()
    fun getFreezeStoreCardFlagValue(): Boolean?
    fun observeResult(viewLifecycleOwner: LifecycleOwner?, content : (Boolean) -> Unit)

}
class StoreCardUpsellMessageFlagState @Inject constructor() : StoreCardUpsellMessageFlag {

    val onUpshellMessageFreezeCardTap : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val onUpshellMessageActivateTempCardTap : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    override fun disableFreezeStoreCardFlag() {
        onUpshellMessageFreezeCardTap.value = false // required to prevent automatic device security popup on landing
    }

    override fun getFreezeStoreCardFlagValue() = onUpshellMessageFreezeCardTap.value

    override fun observeResult(viewLifecycleOwner: LifecycleOwner?, content: (Boolean) -> Unit) {
        viewLifecycleOwner ?: return
        onUpshellMessageFreezeCardTap.observe(viewLifecycleOwner) { isActive ->
            if (isActive)
              content(isActive)
        }
    }

    override fun activateFreezeStoreCardFlag() {
        onUpshellMessageFreezeCardTap.value = true
    }


}