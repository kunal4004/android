package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountInArrearsLandingDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingDialogFragment
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class AccountInArrearsLandingDialogFragment :
    ViewBindingDialogFragment<AccountInArrearsLandingDialogFragmentBinding>() {

    val args: AccountInArrearsLandingDialogFragmentArgs by navArgs()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AccountInArrearsLandingDialogFragmentBinding {
        return AccountInArrearsLandingDialogFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val account = args.account
        val amountOverdue = account?.amountOverdue

        binding.accountInArrearsDescriptionTextView.text = activity?.resources?.getString(
            R.string.payment_overdue_error_desc, Utils.removeNegativeSymbol(
                amountOverdue?.let { amount -> CurrencyFormatter.formatAmountToRandAndCent(amount) })
        )
    }
}

