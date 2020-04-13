package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_status_layout.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryStatusFragment : Fragment() {

    var bundle: Bundle? = null
    var statusResponse: StatusResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_status_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("delivery_status_response"), StatusResponse::class.java) as StatusResponse?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callTheCallCenter?.setOnClickListener { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
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
            }
        }
        deliveryStatusDescription.text = statusResponse?.deliveryStatus?.displayCopy
    }
}