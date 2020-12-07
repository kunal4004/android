package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.processing_request_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.bindString

class PMAProcessYourCardFragment : ProcessYourRequestFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processRequestTitleTextView?.text = bindString(R.string.processing_your_card_label)
    }
}