package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_learn_more_fragment.*

class PayMyAccountLearnMoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pay_my_account_learn_more_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateItem()

    }

    private fun populateItem() {
        val view = View.inflate(activity, R.layout.atm_payment_item_layout, null)
        val indexTextView: TextView? = view?.findViewById(R.id.indexTextView)
        val atmTitleTextView: TextView? = view?.findViewById(R.id.atmTitleTextView)

        atmPaymentItemLinearLayout?.addView(view)
    }
}