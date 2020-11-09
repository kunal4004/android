package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.processing_request_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.bindString

class PMAAddNewCardProgressIndicatorFragment : ProcessYourRequestFragment() {

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processingLayoutTitle?.text = when (payMyAccountViewModel.showCardProgress) {
            true -> bindString(R.string.processing_your_card_label)
            false -> bindString(R.string.processing_your_request)
        }
    }

}