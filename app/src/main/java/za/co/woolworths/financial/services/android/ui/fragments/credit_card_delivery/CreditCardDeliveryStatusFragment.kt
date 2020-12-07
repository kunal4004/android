package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_status_layout.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryStatusFragment : Fragment(), View.OnClickListener {

    var bundle: Bundle? = null
    var navController: NavController? = null
    var statusResponse: StatusResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_status_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.updateStatusBarBackground(activity, R.color.grey_bg)
        navController = Navigation.findNavController(view)
        callTheCallCenter?.setOnClickListener { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                changeToolbarBackground(R.color.grey_bg)
                hideToolbar()
            }
        }
        manageDeliveryLayout.setOnClickListener(this)
        trackDeliveryLayout.setOnClickListener(this)
        configureUI()
    }

    fun configureUI() {
        when (statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(CreditCardDeliveryStatus.DEFAULT)) {
            CreditCardDeliveryStatus.CARD_DELIVERED -> {
                progressIcon.setBackgroundResource(R.drawable.ic_delivered)
                deliveryDate.text = "Delivered"
                deliveryStatusTitle.text = "Your card has been"
            }
            CreditCardDeliveryStatus.CANCELLED -> {
                progressIcon.setBackgroundResource(R.drawable.ic_delivery_tomorrow)
                deliveryDate.text = "Cancelled"
                deliveryStatusTitle.text = "Your card has been"
                callTheCallCenter.visibility = View.VISIBLE
            }
            CreditCardDeliveryStatus.CARD_SHREDDED -> {
                progressIcon.setBackgroundResource(R.drawable.ic_delivery_tomorrow)
                deliveryDate.text = "Failed"
                deliveryStatusTitle.text = "Your Card Delivery Hasn"
                callTheCallCenter.visibility = View.VISIBLE
            }
            CreditCardDeliveryStatus.APPOINTMENT_SCHEDULED -> {
                progressIcon.setBackgroundResource(R.drawable.ic_delivery_later)
                deliveryDate.text = "Tomorrow"
                deliveryStatusTitle.text = "DELIVERY CONFIRMATION"
                manageDeliveryLayout.visibility = View.VISIBLE
                trackDeliveryLayout.visibility = View.VISIBLE
                val manageDeliveryDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_delivery_truck)
                manageDeliveryDrawable?.alpha = 77
                manageDeliveryText.setCompoundDrawablesWithIntrinsicBounds(manageDeliveryDrawable, null, ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_caret_black), null)
                val trackDeliveryDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_directions)
                trackDeliveryDrawable?.alpha = 77
                trackDeliveryText.setCompoundDrawablesWithIntrinsicBounds(trackDeliveryDrawable, null, ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_caret_black), null)
            }
        }
        deliveryStatusDescription.text = statusResponse?.deliveryStatus?.displayCopy
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.manageDeliveryLayout -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_MANAGE_DELIVERY)
                navController?.navigate(R.id.action_to_creditCardDeliveryManageDeliveryFragment, bundleOf("bundle" to bundle))
            }
            R.id.trackDeliveryLayout -> {
                //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_MANAGE_DELIVERY)
            }
        }
    }
}