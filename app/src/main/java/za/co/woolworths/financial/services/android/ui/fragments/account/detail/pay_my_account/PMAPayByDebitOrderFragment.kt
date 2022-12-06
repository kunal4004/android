package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PmaPayByDebitOrderFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class PMAPayByDebitOrderFragment : BaseFragmentBinding<PmaPayByDebitOrderFragmentBinding>(PmaPayByDebitOrderFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.debitOrderDeductionDescTextView?.text = KotlinUtils.highlightText(bindString(R.string.debit_order_deduction_desc), mutableListOf("Note:"))
    }
}