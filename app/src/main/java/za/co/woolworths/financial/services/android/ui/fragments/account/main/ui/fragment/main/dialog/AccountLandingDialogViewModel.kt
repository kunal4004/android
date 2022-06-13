package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.PopUpCommands
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class AccountLandingDialogViewModel @Inject constructor() : ViewModel() {
    var account: MutableLiveData<Account> = MutableLiveData()
    var dialogData: MutableLiveData<DialogData> = MutableLiveData()
    var eligibilityPlan: MutableLiveData<EligibilityPlan> = MutableLiveData()
    val command = SingleLiveEvent<PopUpCommands>()


    fun setup(args: AccountLandingDialogFragmentArgs) {
        dialogData.value = args.dialogData
        eligibilityPlan.value = args.eligibilityPlan
        account.value = args.account
    }

    fun handlePayNowClick() {
        eligibilityPlan.value?.apply {
            when (dialogData.value) {
                DialogData.EliteDialog() -> {
                    when (productGroupCode) {
                        ProductGroupCode.CC -> {
                            command.value = PopUpCommands.TreatPlanSetup
                        }
                        else -> {
                            command.value = PopUpCommands.MakePayment
                        }
                    }
                }
                DialogData.VipDialog() -> {
                    command.value = PopUpCommands.MakePayment
                }
                DialogData.ViewPlanDialog() -> {
                    command.value = PopUpCommands.TreatPlanView
                }
                DialogData.AccountInArrDialog() -> {
                    command.value = PopUpCommands.CallsUs
                }
            }
        }
    }
    fun handleCallUsClick() {
        eligibilityPlan.value?.apply {
            when (dialogData.value) {
                DialogData.EliteDialog(),DialogData.VipDialog() -> {
                    command.value = PopUpCommands.TreatPlanSetup
                }
                DialogData.ViewPlanDialog() -> {
                    command.value = PopUpCommands.MakePayment
                }
                DialogData.AccountInArrDialog() -> {
                    command.value = PopUpCommands.CallsUs
                }
            }
        }
    }

    fun amountOverdue():String{
        val amountOverdue = account.value?.amountOverdue
        return Utils.removeNegativeSymbol(
            amountOverdue?.let { amount -> CurrencyFormatter.formatAmountToRandAndCent(amount) })
    }
}