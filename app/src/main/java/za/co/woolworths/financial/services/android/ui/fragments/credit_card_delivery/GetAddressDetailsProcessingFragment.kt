package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AddressDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.RecipientDetailsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.Utils

class GetAddressDetailsProcessingFragment : BaseProcessingFragment() {

    var envelopeNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        envelopeNumber = bundle?.getString("envelopeNumber", "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //navController = Navigation.findNavController(view)
        //getRecipientDetails()
    }

    /*private fun getRecipientDetails() {
        startProgress()
        envelopeNumber?.let {
            OneAppService.getAddressDetails(it).enqueue(CompletionHandler(object : IResponseListener<AddressDetailsResponse> {
                override fun onSuccess(response: AddressDetailsResponse?) {
                    navigateToRecipientDetailsFragment(response)
                }

                override fun onFailure(error: Throwable?) {
                    super.onFailure(error)
                    navigateToRecipientDetailsFragment()
                }

            }, AddressDetailsResponse::class.java))
        }
    }*/

    /*fun navigateToRecipientDetailsFragment(response: AddressDetailsResponse? = null) {
        response?.let { bundle?.putString("RecipientDetailsResponse", Utils.toJson(it)) }
        navController?.navigate(R.id.action_to_getRecipientDetailsProcessingFragment, bundleOf("bundle" to bundle))
    }*/
}