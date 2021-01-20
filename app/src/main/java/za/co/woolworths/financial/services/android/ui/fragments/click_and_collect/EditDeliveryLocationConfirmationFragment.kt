package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.click_collect_items_limited_message.*
import kotlinx.android.synthetic.main.edit_delivery_location_confirmation_fragment.*
import kotlinx.android.synthetic.main.edit_delivery_location_confirmation_fragment.maxItemsInfoMessageLayout
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class EditDeliveryLocationConfirmationFragment : Fragment() {
    var selectedSuburb: Suburb? = null
    var selectedProvince: Province? = null
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
            selectedProvince = Utils.jsonStringToObject(getString("PROVINCE"), Province::class.java) as Province?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deliveryOptionImage?.setBackgroundResource(if (deliveryType == DeliveryType.STORE_PICKUP) R.drawable.icon_basket else R.drawable.icon_delivery)
        deliveryOption?.text = activity?.resources?.getString(if (deliveryType == DeliveryType.DELIVERY) R.string.delivering_to else R.string.collecting_from)
        suburbName?.text = if (deliveryType == DeliveryType.STORE_PICKUP) activity?.resources?.getString(R.string.store)+selectedSuburb?.name else selectedSuburb?.name + ", " + selectedProvince?.name
        address?.text = if (deliveryType == DeliveryType.STORE_PICKUP) selectedSuburb?.storeAddress?.let { it.address1 + ", " + it.address2 } else ""
        WoolworthsApplication.getClickAndCollect()?.minNumberOfItemsAllowed?.let {
            maxItemsInfoMessage?.text = context?.getString(R.string.click_and_collect_max_items)?.let { msgTxt ->
                String.format(msgTxt, it.toString(), WoolworthsApplication.getClickAndCollect()?.maxNumberOfItemsAllowed
                        ?: "")
            }
        }
        maxItemsInfoMessageLayout?.visibility = if (deliveryType == DeliveryType.STORE_PICKUP && WoolworthsApplication.getClickAndCollect()?.maxNumberOfItemsAllowed != null) View.VISIBLE else View.GONE
        dismissActivity()
    }

    private fun dismissActivity() {
        Handler().postDelayed({
            activity?.apply {
                setResult(RESULT_OK)
                finish()
            }
        }, 2000)
    }
}