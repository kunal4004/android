package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_opt_in_confirmation_fragment.*
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel

class BPIOptInConfirmationFragment : Fragment() {

    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_opt_in_confirmation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmBpiButton?.setOnClickListener {
            val bundle  = Bundle()
            val productOfferingId = bpiViewModel?.mAccount?.productOfferingId?.toString() ?: ""
            bundle.putString("otpMethodType",OTPMethodType.SMS.name)
            bundle.putString("productOfferingId",productOfferingId)
            view.findNavController().navigate(R.id.action_BPIOptInConfirmationFragment_to_sendOtpFragment,
                bundleOf("bundle" to bundle)
            )
        }
    }
}