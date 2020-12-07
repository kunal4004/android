package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.processing_request_fragment.*

class CardProcessYourRequestFragment : ProcessYourRequestFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processRequestTitleTextView?.visibility=View.GONE
        processRequestDescriptionTextView?.visibility = View.GONE
    }
}