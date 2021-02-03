package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_confirm_address_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_get_time_slots_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_invalid_address_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_no_time_slots_available_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_request_layout.*
import kotlinx.android.synthetic.main.npc_processing_request_layout.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.*
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryValidateAddressRequestFragment : CreditCardDeliveryBaseFragment(), ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotView, IProgressAnimationState, View.OnClickListener {

    private var navController: NavController? = null
    private var presenter: ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotPresenter? = null
    private var possibleAddressResponse: PossibleAddressResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_validate_address_request_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ValidateAddressAndTimeSlotPresenterImpl(this, ValidateAddressAndTimeSlotInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse?
        }
        confirmAddress?.setOnClickListener(this)
        editAddress?.setOnClickListener(this)
        contactCourier?.setOnClickListener(this)
        retryOnInvalidAddress?.setOnClickListener(this)
        retryOnValidateAddressFailure?.setOnClickListener(this)
        retryGetTimeSlots?.setOnClickListener(this)
        cancelOnNoTimeSlots?.setOnClickListener(this)
        cancelOnTimeSlotsError?.setOnClickListener(this)
        cancelOnValidateAddress?.setOnClickListener(this)
        if (activity is CreditCardDeliveryActivity) {
            (activity as CreditCardDeliveryActivity)?.apply {
                changeToolbarBackground(R.color.white)
                hideToolbar()
            }
        }
        getValidateAddress()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmAddress -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_DELIVERY_CONFIRM)
                updateAddressDetails()
                getAvailableTimeSlots()
            }
            R.id.editAddress -> {
                activity?.onBackPressed()
            }
            R.id.contactCourier, R.id.callCourierPartner -> {
                activity?.apply { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
            }
            R.id.retryOnValidateAddressFailure, R.id.retryOnInvalidAddress -> {
                activity?.apply {
                    restartProgress()
                    productOfferingId.let { presenter?.initValidateAddress(getSearchPhase(scheduleDeliveryRequest.addressDetails), it, envelopeNumber) }
                }
            }
            R.id.retryGetTimeSlots -> {
                activity?.apply {
                    getAvailableTimeSlots()
                }
            }
            else -> activity?.finish()
        }
    }

    override fun getValidateAddress() {
        startProgress()
        productOfferingId.let { presenter?.initValidateAddress(getSearchPhase(scheduleDeliveryRequest.addressDetails), it, envelopeNumber) }
    }

    override fun getAvailableTimeSlots() {
        activity?.apply {
            possibleAddressResponse?.address?.let {
                restartProgress()
                envelopeNumber.let { it1 ->
                    productOfferingId.let { it2 ->
                        presenter?.initAvailableTimeSlots(it1, it2, it.x, it.y, KotlinUtils.toShipByDateFormat(KotlinUtils.getDateDaysAfter(2)))
                        statusResponse?.addressDetails?.x = it.x
                        statusResponse?.addressDetails?.y = it.y
                        scheduleDeliveryRequest.addressDetails?.x = it.x
                        scheduleDeliveryRequest.addressDetails?.y = it.y
                        bundle?.putString("ScheduleDeliveryRequest", Utils.toJson(scheduleDeliveryRequest))
                        bundle?.putString("StatusResponse", Utils.toJson(statusResponse))
                    }
                }
            }
        }
    }

    override fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        processingLayout?.visibility = View.VISIBLE
        processRequestTitleTextView.text = bindString(R.string.processing_your_request)
    }

    override fun restartProgress() {
        getProgressState()?.restartSpinning()
        processRequestTitleTextView.text = bindString(R.string.checking_available_slots)
        processingLayout?.visibility = View.VISIBLE
        flProgressIndicator.visibility = View.VISIBLE
        hideAllSuccessAndFailureViews()
    }

    override fun onValidateAddressSuccess(possibleAddressResponse: PossibleAddressResponse) {
        this.possibleAddressResponse = possibleAddressResponse
        activity?.apply {
            showConfirmAddressView(possibleAddressResponse.address?.name)
        }

    }

    override fun onValidateAddressFailure() {
        activity?.apply {
            showValidateAddressFailureView()
        }
    }

    override fun onInvalidAddress() {
        activity?.apply {
            showInvalidAddressView()
        }
    }

    override fun onSessionTimeout() {

    }

    override fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAvailableTimeSlotsSuccess(availableTimeSlotsResponse: AvailableTimeSlotsResponse) {
        activity?.apply {
            stopProgress()
            bundle?.putString("available_time_slots", Utils.toJson(availableTimeSlotsResponse.timeslots))
            navController?.navigate(R.id.action_to_creditCardDeliveryPreferedTimeslotFragment, bundleOf("bundle" to bundle))
        }
    }

    override fun onAvailableTimeSlotsFailure() {
        activity?.apply {
            showAvailableTimeSlotsErrorView()
        }
    }

    override fun onNoTimeSlotsAvailable() {
        activity?.apply {
            showNoTimeSlotsAvailableView()
        }
    }

    override fun stopProgress(isFailed: Boolean) {
        activity?.apply {
            getProgressState()?.animateSuccessEnd(false)
            if (!isFailed) {
                flProgressIndicator.visibility = View.GONE
                processingLayout.visibility = View.GONE
            }
            hideAllSuccessAndFailureViews()
        }
    }

    private fun hideAllSuccessAndFailureViews() {
        timeSlotsFailureView.visibility = View.GONE
        validateAddressFailureView.visibility = View.GONE
        invalidAddressView.visibility = View.GONE
        confirmAddressView.visibility = View.GONE
        successIcon.visibility = View.GONE
        noTimeSlotsAvailableView.visibility = View.GONE
    }

    private fun showAvailableTimeSlotsErrorView() {
        stopProgress(true)
        timeSlotsFailureView.visibility = View.VISIBLE
    }

    private fun showInvalidAddressView() {
        stopProgress()
        invalidAddressView.visibility = View.VISIBLE
        successIcon?.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.icon_location)
        }
    }

    private fun showValidateAddressFailureView() {
        stopProgress(true)
        validateAddressFailureView.visibility = View.VISIBLE
    }

    private fun showConfirmAddressView(address: String?) {
        stopProgress()
        confirmAddressView.visibility = View.VISIBLE
        addressToConfirm.text = address
        successIcon?.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.icon_location)
        }
    }

    private fun showNoTimeSlotsAvailableView() {
        stopProgress()
        noTimeSlotsAvailableView.visibility = View.VISIBLE
        successIcon?.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.ic_delivery)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
    }

    //This API should be Fire and forget
    private fun updateAddressDetails() {
        val scheduleDeliveryRequest = ScheduleDeliveryRequest()
        scheduleDeliveryRequest.let {
            it.recipientDetails = this.scheduleDeliveryRequest.recipientDetails
            it.addressDetails = this.scheduleDeliveryRequest.addressDetails
            it.slotDetails = this.scheduleDeliveryRequest.slotDetails
        }
        envelopeNumber.let { request(OneAppService.postScheduleDelivery(productOfferingId, envelopeNumber, false, "", scheduleDeliveryRequest)) }
    }

    private fun getSearchPhase(addressDetails: AddressDetails?): String {
        var searchPhase = ""
        addressDetails?.let {
            searchPhase = "${it.street} ${it.suburb} ${it.city} ${it.postalCode}"
        }
        return searchPhase
    }
}