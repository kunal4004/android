package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.android.material.appbar.AppBarLayout
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.RecipientDetailsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.Utils

class GetRecipientDetailsProcessingFragment : BaseProcessingFragment() {

    var envelopeNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            Utils.updateStatusBarBackground(this, R.color.white)
            findViewById<AppBarLayout>(R.id.appbar)?.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
        bundle = arguments?.getBundle("bundle")
        envelopeNumber = bundle?.getString("envelopeNumber", "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //getRecipientDetails()
    }

    /*private fun getRecipientDetails() {
        startProgress()
        envelopeNumber?.let {
            OneAppService.getRecipientDetails(it).enqueue(CompletionHandler(object : IResponseListener<RecipientDetailsResponse> {
                override fun onSuccess(response: RecipientDetailsResponse?) {
                    navigateToRecipientDetailsFragment(response)
                }

                override fun onFailure(error: Throwable?) {
                    super.onFailure(error)
                    navigateToRecipientDetailsFragment()
                }

            }, RecipientDetailsResponse::class.java))
        }
    }*/

    /*fun navigateToRecipientDetailsFragment(response: RecipientDetailsResponse? = null) {
        response?.let { bundle?.putString("RecipientDetailsResponse", Utils.toJson(it)) }
        navController?.navigate(R.id.action_to_creditCardDeliveryRecipientDetailsFragment, bundleOf("bundle" to bundle))
        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
    }*/
}