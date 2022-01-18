package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Utils

open class CreditCardDeliveryBaseFragment : Fragment() {

    var bundle: Bundle? = null
    lateinit var envelopeNumber: String
    lateinit var productOfferingId: String
    lateinit var scheduleDeliveryRequest: ScheduleDeliveryRequest
    var statusResponse: StatusResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle = arguments?.getBundle(BundleKeysConstants.BUNDLE)

        bundle?.apply {
            envelopeNumber = getString(BundleKeysConstants.ENVELOPE_NUMBER, "")
            productOfferingId = getString(BundleKeysConstants.PRODUCT_OFFERINGID, "")
            if (containsKey("ScheduleDeliveryRequest")) {
                scheduleDeliveryRequest = Utils.jsonStringToObject(getString("ScheduleDeliveryRequest"), ScheduleDeliveryRequest::class.java) as ScheduleDeliveryRequest
            }
            if (containsKey(BundleKeysConstants.STATUS_RESPONSE)) {
                statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse
            }
        }
    }
}