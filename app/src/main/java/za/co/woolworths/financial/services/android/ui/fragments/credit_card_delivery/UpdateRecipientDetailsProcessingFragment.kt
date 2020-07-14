package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.Utils

class UpdateRecipientDetailsProcessingFragment : BaseProcessingFragment() {

    var envelopeNumber: String? = null
    var bookingAddress: BookingAddress? = null
    var productOfferingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")

        bundle?.apply {
            envelopeNumber = getString("envelopeNumber", "")
            productOfferingId = getString("productOfferingId", "")
            if (containsKey("BookingAddress")) {
                bookingAddress = Utils.jsonStringToObject(getString("BookingAddress"), BookingAddress::class.java) as BookingAddress
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        getRecipientDetails()
    }

    private fun getRecipientDetails() {
        val recipientDetails = bookingAddress?.let { RecipientDetails(it.telWork, it.telCell, it.deliverTo, it.idNumber, it.isThirdPartyRecipient) }
        startProgress()
        envelopeNumber?.let {
            OneAppService.updateRecipientDetails(it, UpdateRecipientDetailsRequestBody(recipientDetails, productOfferingId)).enqueue(CompletionHandler(object : IResponseListener<CreditCardDeliveryStatusResponse> {
                override fun onSuccess(response: CreditCardDeliveryStatusResponse?) {
                    navigateToAddressDetailsFragment(response)
                }

                override fun onFailure(error: Throwable?) {
                    super.onFailure(error)
                    navigateToAddressDetailsFragment()
                }

            }, CreditCardDeliveryStatusResponse::class.java))
        }
    }

    fun navigateToAddressDetailsFragment(response: CreditCardDeliveryStatusResponse? = null) {
        response?.let { bundle?.putString("CreditCardDeliveryStatusResponse", Utils.toJson(it)) }
        navController?.navigate(R.id.action_to_creditCardDeliveryAddressDetailsFragment, bundleOf("bundle" to bundle))
    }
}