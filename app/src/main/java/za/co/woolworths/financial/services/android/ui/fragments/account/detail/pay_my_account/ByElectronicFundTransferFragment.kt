package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_by_electronic_fund_transfer_eft_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ByElectronicFundTransferFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_by_electronic_fund_transfer_eft_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateBankingDetail()
    }

    private fun populateBankingDetail() {
        var mPayMyAccountPresenter: PayMyAccountPresenterImpl? = null
        activity?.apply {
            (this as? PayMyAccountActivity)?.apply {
                configureToolbar(bindString(R.string.atm_banking_details))
                displayToolbarDivider(true)
                mPayMyAccountPresenter = getPayMyAccountPresenter()
            }

            mPayMyAccountPresenter?.getElectronicFundTransferBankingDetail()?.forEach { paymentItem ->
                val view = View.inflate(this, R.layout.atm_banking_detail_item, null)
                val paymentName: TextView? = view?.findViewById(R.id.paymentName)
                val paymentValue: TextView? = view?.findViewById(R.id.paymentvalue)
                val accountLabel = KotlinUtils.capitaliseFirstLetter(KotlinUtils.addSpaceBeforeUppercase(paymentItem.key) + ":")
                paymentName?.text = accountLabel
                paymentValue?.text = paymentItem.value
                bankingDetailLinearLayout?.addView(view)
            }
        }
    }
}