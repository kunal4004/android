package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryValidateAddressRequestLayoutBinding
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.*
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryValidateAddressRequestFragment : CreditCardDeliveryBaseFragment(R.layout.credit_card_delivery_validate_address_request_layout), ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotView, IProgressAnimationState, View.OnClickListener {

    private lateinit var binding: CreditCardDeliveryValidateAddressRequestLayoutBinding
    private var navController: NavController? = null
    private var presenter: ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotPresenter? = null
    private var possibleAddressResponse: PossibleAddressResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ValidateAddressAndTimeSlotPresenterImpl(this, ValidateAddressAndTimeSlotInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreditCardDeliveryValidateAddressRequestLayoutBinding.bind(view)

        navController = Navigation.findNavController(view)
        bundle?.apply {
            statusResponse = getParcelable(BundleKeysConstants.STATUS_RESPONSE)
        }

        binding.apply {
            confirmAddressView.apply {
                confirmAddress?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
                editAddress?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
            }
            invalidAddressView.apply {
                contactCourier?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
                retryOnInvalidAddress?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
            }
            validateAddressFailureView.apply {
                retryOnValidateAddressFailure?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
                cancelOnValidateAddress?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
            }
            timeSlotsFailureView.apply {
                retryGetTimeSlots?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
                cancelOnTimeSlotsError?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
            }
            noTimeSlotsAvailableView.apply {
                cancelOnNoTimeSlots?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
                callCourierPartner?.setOnClickListener(this@CreditCardDeliveryValidateAddressRequestFragment)
            }
            if (activity is CreditCardDeliveryActivity) {
                (activity as CreditCardDeliveryActivity)?.apply {
                    changeToolbarBackground(R.color.white)
                    hideToolbar()
                }
            }
            getValidateAddress()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmAddress -> {
                (activity as? CreditCardDeliveryActivity)?.mFirebaseCreditCardDeliveryEvent?.forCreditCardDeliveryConfirm()
                updateAddressDetails()
                getAvailableTimeSlots()
            }
            R.id.editAddress -> {
                activity?.onBackPressed()
            }
            R.id.contactCourier, R.id.callCourierPartner -> {
                activity?.apply { Utils.makeCall(AppConfigSingleton.creditCardDelivery?.callCenterNumber) }
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
                        bundle?.putParcelable(BundleKeysConstants.STATUS_RESPONSE, statusResponse)
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
        binding.apply {
            processingLayout?.root?.visibility = View.VISIBLE
            processingLayout.processRequestTitleTextView.text = bindString(R.string.processing_your_request)
        }
    }

    override fun restartProgress() {
        binding.apply {
            getProgressState()?.restartSpinning()
            processingLayout.processRequestTitleTextView.text = bindString(R.string.checking_available_slots)
            processingLayout?.root?.visibility = View.VISIBLE
            flProgressIndicator.visibility = View.VISIBLE
            hideAllSuccessAndFailureViews()
        }
    }

    override fun onValidateAddressSuccess(possibleAddressResponse: PossibleAddressResponse) {
        this.possibleAddressResponse = possibleAddressResponse
        activity?.apply {
            binding.showConfirmAddressView(possibleAddressResponse.address?.name)
        }

    }

    override fun onValidateAddressFailure() {
        activity?.apply {
            binding.showValidateAddressFailureView()
        }
    }

    override fun onInvalidAddress() {
        activity?.apply {
            binding.showInvalidAddressView()
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
            binding.showAvailableTimeSlotsErrorView()
        }
    }

    override fun onNoTimeSlotsAvailable() {
        activity?.apply {
            binding.showNoTimeSlotsAvailableView()
        }
    }

    override fun stopProgress(isFailed: Boolean) {
        activity?.apply {
            getProgressState()?.animateSuccessEnd(false)
            if (!isFailed) {
                binding.flProgressIndicator.visibility = View.GONE
                binding.processingLayout.root.visibility = View.GONE
            }
            binding.hideAllSuccessAndFailureViews()
        }
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.hideAllSuccessAndFailureViews() {
        timeSlotsFailureView.root.visibility = View.GONE
        validateAddressFailureView.root.visibility = View.GONE
        invalidAddressView.root.visibility = View.GONE
        confirmAddressView.root.visibility = View.GONE
        successIcon.visibility = View.GONE
        noTimeSlotsAvailableView.root.visibility = View.GONE
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.showAvailableTimeSlotsErrorView() {
        stopProgress(true)
        timeSlotsFailureView.root.visibility = View.VISIBLE
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.showInvalidAddressView() {
        stopProgress()
        invalidAddressView.root.visibility = View.VISIBLE
        successIcon?.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.icon_location)
        }
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.showValidateAddressFailureView() {
        stopProgress(true)
        validateAddressFailureView.root.visibility = View.VISIBLE
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.showConfirmAddressView(address: String?) {
        stopProgress()
        confirmAddressView.root.visibility = View.VISIBLE
        confirmAddressView.addressToConfirm.text = address
        successIcon?.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.icon_location)
        }
    }

    private fun CreditCardDeliveryValidateAddressRequestLayoutBinding.showNoTimeSlotsAvailableView() {
        stopProgress()
        noTimeSlotsAvailableView.root.visibility = View.VISIBLE
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