package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryManageDeliveryBinding
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class CreditCardDeliveryManageDeliveryFragment : BaseFragmentBinding<CreditCardDeliveryManageDeliveryBinding>(CreditCardDeliveryManageDeliveryBinding::inflate), View.OnClickListener {

    var bundle: Bundle? = null
    private var statusResponse: StatusResponse? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.init()
        setupToolbar()
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity?)?.supportActionBar?.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancelDelivery -> {
                activity?.apply {
                    (this as? CreditCardDeliveryActivity)?.mFirebaseCreditCardDeliveryEvent?.forCreditCarDeliveryCancel()
                    supportFragmentManager.apply {
                        val creditCardCancelDeliveryFragment = CancelOrToLateDeliveryDialog.newInstance(CreditCardDeliveryActivity.DeliveryStatus.CANCEL_DELIVERY)
                        creditCardCancelDeliveryFragment.show(this, CancelOrToLateDeliveryDialog::class.java.simpleName)
                    }
                }
            }
            R.id.editAddress -> {
                if (statusResponse?.deliveryStatus?.isEditable == true) {
                    val bundle = bundleOf(
                            "bundle" to bundle,
                            "isEditRecipient" to true)
                    navController?.navigate(R.id.action_creditCardDeliveryAddressDetailsFragment, bundle)
                } else {
                    activity?.apply {
                        supportFragmentManager.apply {
                            val creditCardCancelDeliveryFragment = CancelOrToLateDeliveryDialog.newInstance(CreditCardDeliveryActivity.DeliveryStatus.EDIT_ADDRESS)
                            creditCardCancelDeliveryFragment.show(this, CancelOrToLateDeliveryDialog::class.java.simpleName)
                        }
                    }
                }
            }
            R.id.editRecipient -> {
                val bundle = bundleOf(
                        "bundle" to bundle,
                        "isEditRecipient" to true)
                navController?.navigate(R.id.creditCardDeliveryRecipientDetailsFragment, bundle)
            }
        }
    }

    private fun CreditCardDeliveryManageDeliveryBinding.init() {
        bundle?.apply {
            statusResponse = getParcelable(BundleKeysConstants.STATUS_RESPONSE)
        }
        editAddress?.setOnClickListener(this@CreditCardDeliveryManageDeliveryFragment)
        cancelDelivery?.setOnClickListener(this@CreditCardDeliveryManageDeliveryFragment)
        editRecipient?.setOnClickListener(this@CreditCardDeliveryManageDeliveryFragment)
        splitAndApplyFormatedDate()
    }

    private fun setupToolbar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle(bindString(R.string.manage_delivery_title))
                changeToolbarBackground(R.color.white)
            }
        }
    }

    private fun CreditCardDeliveryManageDeliveryBinding.splitAndApplyFormatedDate() {
        statusResponse?.slotDetails?.slot.also { deliveryTime.text = it }
        WFormatter.getDayAndFullDate(statusResponse?.slotDetails?.appointmentDate).also { deliveryDate.text = it }
        statusResponse?.addressDetails?.searchPhrase.also { deliveryAddress.text = it }
        statusResponse?.recipientDetails?.deliverTo.also { name.text = it }
        if (statusResponse?.bookingreference == null) {
            if (statusResponse?.appointment?.bookingReference != null) {
                bookingReference.text = statusResponse?.appointment?.bookingReference
            }
        } else {
            bookingReference.text = statusResponse?.bookingreference
        }
        WFormatter.getFullMonthDate(statusResponse?.deliveryStatus?.receivedDate).also { createdDate.text = it }
    }
}