package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_schedule_delivery_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_schedule_delivery_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_request_layout.processingLayout
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryScheduleDeliveryFragment : CreditCardDeliveryBaseFragment(), ScheduleDeliveryContract.ScheduleDeliverView, IProgressAnimationState, View.OnClickListener {

    private var navController: NavController? = null
    private var presenter: ScheduleDeliveryContract.ScheduleDeliveryPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_schedule_delivery_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ScheduleDeliveryPresenterImpl(this, ScheduleDeliveryInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        retryScheduleDeliveryBtn.setOnClickListener(this)
        callCourierPartner.setOnClickListener(this)
        postScheduleDelivery()
    }

    override fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        processingLayout?.visibility = View.VISIBLE
    }

    override fun onScheduleDeliverySuccess(creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse) {
        activity?.apply {
            getProgressState()?.animateSuccessEnd(true)
            Handler().postDelayed({
                processingLayout?.visibility = View.GONE
                if (bundle?.containsKey("isEditAddressActivity") == true) {
                    if (bundle?.getBoolean("isEditAddressActivity") == true) {
                        scheduleDeliveryUpdateSuccessView.visibility = View.VISIBLE
                    } else
                        scheduleDeliverySuccessView.visibility = View.VISIBLE
                } else {
                    scheduleDeliverySuccessView.visibility = View.VISIBLE
                }
            }, 1000)
            Handler().postDelayed({
                navController?.navigate(R.id.action_to_creditCardDeliveryStatusFragment, bundleOf("bundle" to bundle))
            }, 4000)
        }
    }

    override fun onScheduleDeliveryFailure() {
        activity?.apply {
            getProgressState()?.animateSuccessEnd(false)
            processingLayout?.visibility = View.GONE
            scheduleDeliveryFailureView.visibility = View.VISIBLE
        }
    }

    override fun onSessionTimeout() {

    }

    override fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun postScheduleDelivery() {
        activity?.apply {
            scheduleDeliveryRequest.let {
                startProgress()
                if (isEditRecipient() == true) {
                    presenter?.initScheduleDelivery(productOfferingId, envelopeNumber, false, "", it)
                } else
                    presenter?.initScheduleDelivery(productOfferingId, envelopeNumber, true, "", it)
            }
        }
    }

    override fun retryScheduleDelivery() {
        activity?.apply {
            scheduleDeliveryFailureView.visibility = View.GONE
            getProgressState()?.restartSpinning()
            processingLayout?.visibility = View.VISIBLE
            scheduleDeliveryRequest.let {
                if (isEditRecipient() == true) {
                    presenter?.initScheduleDelivery(productOfferingId, envelopeNumber, false, "", it)
                } else
                    presenter?.initScheduleDelivery(productOfferingId, envelopeNumber, true, "", it)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.retryScheduleDeliveryBtn -> retryScheduleDelivery()
            R.id.callCourierPartner -> activity?.apply { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
        }
    }

    private fun isEditRecipient(): Boolean {
        var isEdited: Boolean
        if (bundle?.containsKey("isEditRecipientActivity") == true) {
            isEdited = bundle?.getBoolean("isEditRecipientActivity") == true
        } else
            isEdited = false
        return isEdited
    }
}