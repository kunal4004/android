package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.bindString

class PMAProcessYourCardFragment : ProcessYourRequestFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.processRequestNavHostFragment.includePMAProcessing.processRequestTitleTextView?.text = bindString(R.string.processing_your_card_label)
    }
}