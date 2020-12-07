package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryManageDeliveryFragment : Fragment(), View.OnClickListener {

    var bundle: Bundle? = null
    var statusResponse: StatusResponse? = null

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
        setupToolbar()
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity?)?.supportActionBar?.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancelDelivery -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_MANAGE_DELIVERY_CANCEL)
            }
        }
    }

    private fun setupToolbar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle(bindString(R.string.manage_delivery_title))
                changeToolbarBackground()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as? CreditCardDeliveryActivity)?.hideToolbar()
    }
}