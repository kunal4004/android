package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_base_processing_fragment.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils

class UpdateRecipientDetailsProcessingFragment : CreditCardDeliveryBaseFragment(), IProgressAnimationState {

    var navController: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        getRecipientDetails()
    }

    private fun getRecipientDetails() {
        val scheduleDeliveryRequest = ScheduleDeliveryRequest()
        scheduleDeliveryRequest.bookingAddress = scheduleDeliveryRequest.bookingAddress
        startProgress()
        OneAppService.postScheduleDelivery(productOfferingId, envelopeNumber, false, "", scheduleDeliveryRequest).enqueue(CompletionHandler(object : IResponseListener<CreditCardDeliveryStatusResponse> {
            override fun onSuccess(response: CreditCardDeliveryStatusResponse?) {
                navigateToAddressDetailsFragment(response)
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
                navigateToAddressDetailsFragment()
            }

        }, CreditCardDeliveryStatusResponse::class.java))
    }

    fun navigateToAddressDetailsFragment(response: CreditCardDeliveryStatusResponse? = null) {
        response?.let { bundle?.putString("CreditCardDeliveryStatusResponse", Utils.toJson(it)) }
        navController?.navigate(R.id.action_to_creditCardDeliveryAddressDetailsFragment, bundleOf("bundle" to bundle))
        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
    }

    fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        activationProcessingLayout?.visibility = View.VISIBLE
    }

    fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

}