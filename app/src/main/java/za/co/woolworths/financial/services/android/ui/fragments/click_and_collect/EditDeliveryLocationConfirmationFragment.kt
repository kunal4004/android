package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class EditDeliveryLocationConfirmationFragment : Fragment() {
    var selectedSuburb: Suburb? = null
    var deliveryType: DeliveryType? = null
    var bundle: Bundle? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_delivery_location_confirmation_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            deliveryType = DeliveryType.valueOf(getString(EditDeliveryLocationActivity.DELIVERY_TYPE, DeliveryType.DELIVERY.name))
            selectedSuburb = Utils.jsonStringToObject(getString("SUBURB"), Suburb::class.java) as Suburb?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dismissActivity()
    }

    private fun dismissActivity() {
        Handler().postDelayed({
            activity?.onBackPressed()
        }, 2000)
    }
}