package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.PopUpCommands
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanImpl
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class AccountLandingDialogViewModel @Inject constructor() : ViewModel() {
    var account: MutableLiveData<Account> = MutableLiveData()
    var dialogData: MutableLiveData<DialogData> = MutableLiveData()
    var eligibilityPlan: MutableLiveData<EligibilityPlan> = MutableLiveData()
    val command = SingleLiveEvent<PopUpCommands>()
    var mTreatmentPlanImpl : ViewTreatmentPlanImpl? = null

    fun setup(args: AccountLandingDialogFragmentArgs) {
        dialogData.value = args.dialogData
        eligibilityPlan.value = args.eligibilityPlan
        account.value = args.account

        mTreatmentPlanImpl = ViewTreatmentPlanImpl(
            eligibilityPlan = eligibilityPlan.value,
            account = account.value,
            applyNowState = ApplyNowState.STORE_CARD)
    }

    fun handlePayNowClick() {
        eligibilityPlan.value?.apply {
            when (dialogData.value) {
                is DialogData.EliteDialog -> {
                    when (productGroupCode) {
                        ProductGroupCode.CC -> {
                            command.value = PopUpCommands.TreatPlanSetup
                        }
                        else -> {
                            command.value = PopUpCommands.MakePayment
                        }
                    }
                }
               is DialogData.VipDialog -> {
                    command.value = PopUpCommands.MakePayment
                }
               is DialogData.ViewPlanDialog -> {
                    command.value = PopUpCommands.TreatPlanView
                }
              is  DialogData.AccountInArrDialog -> {
                    command.value = PopUpCommands.CallsUs
                }
                else -> Unit
            }
        }
    }
    fun handleCallUsClick() {
        eligibilityPlan.value?.apply {
            when (dialogData.value) {
                is DialogData.EliteDialog,is DialogData.VipDialog -> command.value = PopUpCommands.TreatPlanSetup
                is DialogData.ViewPlanDialog ->  command.value = PopUpCommands.MakePayment
                is DialogData.AccountInArrDialog -> command.value = PopUpCommands.CallsUs
                else -> Unit
            }
        }
    }

    fun amountOverdue():String{
        val amountOverdue = account.value?.amountOverdue
        return Utils.removeNegativeSymbol(
            amountOverdue?.let { amount -> CurrencyFormatter.formatAmountToRandAndCent(amount) })
    }
}