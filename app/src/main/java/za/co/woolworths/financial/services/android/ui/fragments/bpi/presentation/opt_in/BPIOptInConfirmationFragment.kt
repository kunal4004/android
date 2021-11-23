package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_opt_in_confirmation_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.util.Utils

class BPIOptInConfirmationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_opt_in_confirmation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmBpiButton?.setOnClickListener {
            arguments?.getString(BalanceProtectionInsuranceActivity.BPI_PRODUCT_GROUP_CODE).let { productGroupCode ->
                val bpiTaggingEventCode = when (productGroupCode) {
                    AccountsProductGroupCode.CREDIT_CARD.groupCode -> FirebaseManagerAnalyticsProperties.CC_BPI_OPT_IN_CONFIRM
                    AccountsProductGroupCode.STORE_CARD.groupCode -> FirebaseManagerAnalyticsProperties.SC_BPI_OPT_IN_CONFIRM
                    AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> FirebaseManagerAnalyticsProperties.PL_BPI_OPT_IN_CONFIRM
                    else -> null
                }
                bpiTaggingEventCode?.let { Utils.triggerFireBaseEvents(it, activity) }
            }
        }
    }
}