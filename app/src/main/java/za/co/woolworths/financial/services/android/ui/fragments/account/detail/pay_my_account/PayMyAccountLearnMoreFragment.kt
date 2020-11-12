package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_learn_more_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.KotlinUtils

class PayMyAccountLearnMoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pay_my_account_learn_more_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateATMPaymentItem()
        noteStringBuilder()
    }

    private fun noteStringBuilder() {
        val note = KotlinUtils.highlightText(bindString(R.string.atm_payment_note), "Note")
        atmPaymentNoteTextView?.text = note
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
                atmPaymentItemLinearLayout?.addView(view)
            }
        }
    }
}