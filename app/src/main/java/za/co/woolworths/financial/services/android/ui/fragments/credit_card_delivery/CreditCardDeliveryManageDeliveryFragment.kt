package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_manage_delivery.*
import kotlinx.android.synthetic.main.credit_card_delivery_manage_delivery.deliveryDate
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class CreditCardDeliveryManageDeliveryFragment : Fragment(), View.OnClickListener {

    var bundle: Bundle? = null
    private var statusResponse: StatusResponse? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_manage_delivery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        init()
        setupToolbar()
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity?)?.supportActionBar?.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancelDelivery -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_MANAGE_DELIVERY_CANCEL)
                activity?.apply {
                    supportFragmentManager?.apply {
                        val creditCardCancelDeliveryFragment = CreditCardCancelDeliveryFragment.newInstance(CreditCardDeliveryActivity.DeliveryStatus.CANCEL_DELIVERY)
                        creditCardCancelDeliveryFragment.show(this, CreditCardCancelDeliveryFragment::class.java.simpleName)
                    }
                }
            }
            R.id.editAddress -> {
                if (statusResponse?.deliveryStatus?.isEditable == true) {
                    //navController?.navigate(R.id.action_to_creditCardDeliveryValidateAddressRequestFragment, bundleOf("bundle" to bundle))
                } else {
                    activity?.apply {
                        supportFragmentManager?.apply {
                            val creditCardCancelDeliveryFragment = CreditCardCancelDeliveryFragment.newInstance(CreditCardDeliveryActivity.DeliveryStatus.EDIT_ADDRESS)
                            creditCardCancelDeliveryFragment.show(this, CreditCardCancelDeliveryFragment::class.java.simpleName)
                        }
                    }
                }
            }
            R.id.editRecipient -> {

            }
        }
    }

    private fun init() {
        editAddress?.setOnClickListener(this)
        cancelDelivery?.setOnClickListener(this)

        if (statusResponse?.deliveryStatus?.isCardNew == true) {
            editRecipient?.visibility = View.GONE
        } else {
            editRecipient?.setOnClickListener(this)
        }
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

    private fun splitAndApplyFormatedDate() {
        statusResponse?.slotDetails?.slot.also { deliveryTime.text = it }
        WFormatter.getDayAndFullDate(statusResponse?.slotDetails?.appointmentDate).also { deliveryDate.text = it }
        statusResponse?.addressDetails?.deliveryAddress.also { deliveryAddress.text = it }
        statusResponse?.recipientDetails?.deliverTo.also { name.text = it }
        statusResponse?.bookingreference.also { bookingReference.text = it }
        statusResponse?.deliveryStatus?.receivedDate.also { createdDate.text = it }
    }
}