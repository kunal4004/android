package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_confirm_address_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_get_time_slots_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_invalid_address_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_no_time_slots_available_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_request_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryValidateAddressRequestFragment : Fragment(), CreditCardDeliveryContract.CreditCardDeliveryView, IProgressAnimationState, View.OnClickListener {

    private var navController: NavController? = null
    var bundle: Bundle? = null
    var presenter: CreditCardDeliveryContract.CreditCardDeliveryPresenter? = null
    var possibleAddressResponse: PossibleAddressResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_validate_address_request_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CreditCardDeliveryPresenterImpl(this, CreditCardDeliveryInteractorImpl())
        bundle = arguments?.getBundle("bundle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        confirmAddress?.setOnClickListener(this)
        editAddress?.setOnClickListener(this)
        contactCourier?.setOnClickListener(this)
        retryOnInvalidAddress?.setOnClickListener(this)
        retryOnValidateAddressFailure?.setOnClickListener(this)
        retryGetTimeSlots?.setOnClickListener(this)
        cancelOnNoTimeSlots?.setOnClickListener(this)
        cancelOnTimeSlotsError?.setOnClickListener(this)
        cancelOnValidateAddress?.setOnClickListener(this)
        getValidateAddress()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmAddress -> {
                getAvailableTimeSlots()
            }
            R.id.editAddress -> {
                activity?.onBackPressed()
            }
            R.id.contactCourier, R.id.callCourierPartner -> {
                activity?.apply { Utils.makeCall("0861 50 20 20") }
            }
            R.id.retryOnValidateAddressFailure, R.id.retryOnInvalidAddress -> {
                activity?.apply {
                    restartProgress()
                    presenter?.initValidateAddress("woodstock", "20")
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
        presenter?.initValidateAddress("woodstock", "20")
    }

    override fun getAvailableTimeSlots() {
        activity?.apply {
            possibleAddressResponse?.address?.let {
                restartProgress()
                presenter?.initAvailableTimeSlots("24/03 WOOP 100001", "20", it.x, it.y, "2020-04-08")
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
    }

    override fun restartProgress() {
        getProgressState()?.restartSpinning()
        processingLayout?.visibility = View.VISIBLE
        flProgressIndicator.visibility = View.VISIBLE
        hideAllSuccessAndFailureViews()
    }

    override fun onValidateAddressSuccess(possibleAddressResponse: PossibleAddressResponse) {
        this.possibleAddressResponse = possibleAddressResponse
        activity?.apply {
            showConfirmAddressView()
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
        addressSuccessIcon.visibility = View.GONE
        noTimeSlotsAvailableView.visibility = View.GONE
    }

    fun showAvailableTimeSlotsErrorView() {
        stopProgress(true)
        timeSlotsFailureView.visibility = View.VISIBLE
    }

    private fun showInvalidAddressView() {
        stopProgress()
        invalidAddressView.visibility = View.VISIBLE
        addressSuccessIcon.visibility = View.VISIBLE
    }

    fun showValidateAddressFailureView() {
        stopProgress(true)
        validateAddressFailureView.visibility = View.VISIBLE
    }

    fun showConfirmAddressView() {
        stopProgress()
        confirmAddressView.visibility = View.VISIBLE
        addressSuccessIcon.visibility = View.VISIBLE
    }

    fun showNoTimeSlotsAvailableView() {
        stopProgress()
        noTimeSlotsAvailableView.visibility = View.VISIBLE
        addressSuccessIcon.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getProgressState()?.let { activity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commitAllowingStateLoss() }
    }
}