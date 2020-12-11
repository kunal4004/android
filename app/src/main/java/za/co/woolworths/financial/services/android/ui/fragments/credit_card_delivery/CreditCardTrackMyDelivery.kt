package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_track_my_delivery.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class CreditCardTrackMyDelivery : WBottomSheetDialogFragment(), View.OnClickListener {

    var bundle: Bundle? = null
    private var envelopeNumber: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.credit_card_track_my_delivery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    companion object {
        fun newInstance(bundle: Bundle, envelopeNumber: String) = CreditCardTrackMyDelivery().withArgs {
            putBundle("bundle", bundle)
            putString("envelopeNumber", envelopeNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        envelopeNumber = arguments?.getString("envelopeNumber", "")
    }

    private fun init() {
        referenceNumber.text = envelopeNumber
        referenceNumberText.setOnClickListener(this)
        trackMyDelivery.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.trackMyDelivery -> {
                KotlinUtils.openBrowserWithUrl(WoolworthsApplication.getCreditCardDelivery().deliveryTrackingUrl, activity)
            }
            R.id.referenceNumberText -> {
                val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(referenceNumber.text, referenceNumber.text)
                clipboard?.setPrimaryClip(clip)
            }
        }
    }
}