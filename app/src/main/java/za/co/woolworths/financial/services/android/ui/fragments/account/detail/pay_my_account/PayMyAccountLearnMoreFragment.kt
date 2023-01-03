package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PayMyAccountLearnMoreFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class PayMyAccountLearnMoreFragment : BaseFragmentBinding<PayMyAccountLearnMoreFragmentBinding>(PayMyAccountLearnMoreFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateATMPaymentItem()
        noteStringBuilder()
    }

    private fun noteStringBuilder() {
        val note = KotlinUtils.highlightText(bindString(R.string.atm_payment_note), mutableListOf("Note:"))
        binding.atmPaymentNoteTextView?.text = note
    }

    private fun populateATMPaymentItem() {
        var mPayMyAccountPresenter: PayMyAccountPresenterImpl? = null
        activity?.apply {
            (this as? PayMyAccountActivity)?.apply {
                configureToolbar(bindString(R.string.atm_payment_steps_title))
                displayToolbarDivider(true)
                mPayMyAccountPresenter = getPayMyAccountPresenter()
            }
            mPayMyAccountPresenter?.getATMPaymentInfo()?.forEachIndexed { index, description ->
                val view = View.inflate(this, R.layout.atm_payment_item_layout, null)
                val indexTextView: TextView? = view?.findViewById(R.id.indexTextView)
                val atmTitleTextView: TextView? = view?.findViewById(R.id.atmTitleTextView)
                indexTextView?.text = index.plus(1).toString()
                atmTitleTextView?.text = bindString(description)
                binding.atmPaymentItemLinearLayout?.addView(view)
            }
        }
    }
}