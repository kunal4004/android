package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_track_my_delivery.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardTrackMyDelivery : WBottomSheetDialogFragment(), View.OnClickListener {

    var bundle: Bundle? = null
    private var statusResponse: StatusResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.credit_card_track_my_delivery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    companion object {
        fun newInstance(bundle: Bundle) = CreditCardTrackMyDelivery().withArgs {
            putBundle("bundle", bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("bundle")) {
                bundle = arguments?.getBundle("bundle")
                bundle?.apply {
                    statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse?
                }
            }
        }
    }

    private fun init() {
        if (statusResponse?.bookingreference == null) {
            if (statusResponse?.appointment?.bookingReference != null) {
                referenceNumber.text = statusResponse?.appointment?.bookingReference
            }
        } else {
            referenceNumber.text = statusResponse?.bookingreference
        }
        referenceNumberText.setOnClickListener(this)
        trackMyDelivery.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.trackMyDelivery -> {
                dismiss()
            }
            R.id.referenceNumberText -> {

            }
        }
    }
}