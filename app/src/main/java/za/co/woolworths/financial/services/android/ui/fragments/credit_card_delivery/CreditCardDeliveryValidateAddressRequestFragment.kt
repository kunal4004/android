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
import kotlinx.android.synthetic.main.credit_card_delivery_invalid_address_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_validate_address_request_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery.dialogs.CreditCardDeliveryNoTimeSlotsAvailableDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryValidateAddressRequestFragment : Fragment(), CreditCardDeliveryContract.CreditCardDeliveryView, IProgressAnimationState, View.OnClickListener {

    private var navController: NavController? = null
    var bundle: Bundle? = null
    var presenter: CreditCardDeliveryContract.CreditCardDeliveryPresenter? = null
    var availableTimeSlotsResponse: AvailableTimeSlotsResponse? = null
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
        confirmAddress.setOnClickListener(this)
        editAddress.setOnClickListener(this)
        contactCourier.setOnClickListener(this)
        retry.setOnClickListener(this)
        initValidateAddress()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmAddress -> {
                if (availableTimeSlotsResponse?.timeslots.isNullOrEmpty())
                    activity?.supportFragmentManager?.let { CreditCardDeliveryNoTimeSlotsAvailableDialogFragment.newInstance().show(it, CreditCardDeliveryNoTimeSlotsAvailableDialogFragment::class.java.simpleName) }
                else {
                    navController?.navigate(R.id.action_to_creditCardDeliveryPreferedTimeslotFragment, bundleOf("bundle" to bundle))
                }
            }
            R.id.editAddress -> {

            }
            R.id.contactCourier -> {
                activity?.apply { Utils.makeCall("0861 50 20 20") }
            }
            R.id.retry -> {

            }
        }
    }

    override fun initValidateAddress() {
        startProgress()
        presenter?.initValidateAddress("woodstock", "20")
    }

    override fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        processingLayout?.visibility = View.VISIBLE
    }

    override fun onValidateAddressSuccess(possibleAddressResponse: PossibleAddressResponse) {
        this.possibleAddressResponse = possibleAddressResponse
        possibleAddressResponse.address?.let {
            presenter?.initAvailableTimeSlots("24/03 WOOP 100001", "20", it.x, it.y, "2020-04-08")
        }
    }

    override fun onValidateAddressFailure() {

    }

    override fun onInvalidAddress() {
        stopProgress()
        addressSuccessIcon.visibility = View.VISIBLE
        invalidAddressView.visibility = View.VISIBLE
    }

    override fun onSessionTimeout() {

    }

    override fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAvailableTimeSlotsSuccess(availableTimeSlotsResponse: AvailableTimeSlotsResponse) {
        this.availableTimeSlotsResponse = availableTimeSlotsResponse
        stopProgress()
        addressSuccessIcon.visibility = View.VISIBLE
        confirmAddressView.visibility = View.VISIBLE
    }

    override fun onAvailableTimeSlotsFailure() {

    }

    override fun onNoTimeSlotsAvailable() {

    }

    override fun stopProgress() {
        getProgressState()?.animateSuccessEnd(true)
        flProgressIndicator.visibility = View.GONE
        processingLayout.visibility = View.GONE
    }
}