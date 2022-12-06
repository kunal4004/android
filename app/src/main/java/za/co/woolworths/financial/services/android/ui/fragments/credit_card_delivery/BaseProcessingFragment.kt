package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState

open class BaseProcessingFragment : Fragment(R.layout.credit_card_delivery_base_processing_fragment), IProgressAnimationState {
    var bundle: Bundle? = null
}