package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import javax.inject.Inject

@HiltViewModel
class AccountLandingDialogViewModel @Inject constructor() : ViewModel() {
    var account: Account? = null
    var dialogData: DialogData? = null
    var eligibilityPlan: EligibilityPlan? = null

    fun setup(args: AccountLandingDialogFragmentArgs) {
        dialogData = args.dialogData
        eligibilityPlan = args.eligibilityPlan
        account = args.account
    }
}
